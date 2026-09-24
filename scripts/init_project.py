#!/usr/bin/env python3
"""Rename the AndroidComposeBase starter for a new application.

Run from any directory. This script intentionally refuses already initialized or
unrecognized checkouts so a partial replacement cannot look like success.
"""

from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Iterable

SOURCE_NAME = "AndroidComposeBase"
SOURCE_APP_PACKAGE = "com.thanhng224.androidcomposebase"
SOURCE_CORE_PACKAGE = f"{SOURCE_APP_PACKAGE}.core"
SOURCE_PLUGIN_PACKAGE = "androidcomposebase.buildlogic"
METADATA_LOGGER_MARKERS = (
    "internal class MetadataLoggingInterceptor(",
    'const val TAG = "NetworkMetadata"',
    "Log.d(TAG, it)",
)
IGNORED_PARTS = {
    ".git", ".gradle", ".idea", ".kotlin", "build", "generated", "intermediates",
    "outputs", "reports", "test-results", "__pycache__",
}
IGNORED_SUFFIXES = {".aar", ".bin", ".class", ".ico", ".jar", ".jpg", ".jpeg", ".png", ".pyc", ".so", ".webp"}


class InitError(RuntimeError):
    """Expected validation or transformation failure."""


def valid_project_name(value: str) -> bool:
    return bool(re.fullmatch(r"[A-Z][A-Za-z0-9_]*", value))


def valid_package(value: str) -> bool:
    if not re.fullmatch(r"[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+", value):
        return False
    keywords = {
        "abstract", "as", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
        "false", "final", "finally", "float", "for", "fun", "goto", "if", "implements", "in",
        "instanceof", "int", "interface", "is", "long", "native", "new", "null", "object",
        "package", "private", "protected", "public", "return", "short", "static", "super",
        "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typealias",
        "typeof", "val", "var", "void", "volatile", "when", "while",
    }
    return not any(part in keywords or part == "_" for part in value.split("."))


def escape_android_string_resource(value: str) -> str:
    """Escape Android string syntax; ElementTree separately escapes XML markup."""
    escaped = value.replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"')
    if escaped.startswith(("@", "?")):
        escaped = "\\" + escaped
    return escaped


def _is_ignored(path: Path, root: Path) -> bool:
    relative = path.relative_to(root)
    if relative.parts[:2] == ("docs", "archive"):
        return True
    if set(relative.parts) & IGNORED_PARTS or path.suffix.lower() in IGNORED_SUFFIXES:
        return True
    return any(part.startswith(".") and part != ".github" for part in relative.parts[:-1])


def _tracked_or_existing(root: Path, source: Path, destination: Path) -> None:
    if not source.exists():
        return
    if destination.exists() and source.resolve() != destination.resolve():
        raise InitError(f"Refusing to overwrite existing path: {destination.relative_to(root)}")


def _move(root: Path, source: Path, destination: Path, dry_run: bool) -> bool:
    if not source.exists() or source.resolve() == destination.resolve():
        return False
    _tracked_or_existing(root, source, destination)
    if dry_run:
        print(f"  move {source.relative_to(root)} -> {destination.relative_to(root)}")
        return True
    destination.parent.mkdir(parents=True, exist_ok=True)
    if (root / ".git").exists():
        try:
            subprocess.run(["git", "mv", str(source), str(destination)], cwd=root, check=True, capture_output=True)
            return True
        except (OSError, subprocess.CalledProcessError):
            pass
    if destination.exists():
        raise InitError(f"Destination appeared during move: {destination.relative_to(root)}")
    shutil.move(str(source), str(destination))
    return True


def _source_markers(root: Path) -> list[Path]:
    return [
        root / "settings.gradle.kts",
        root / "app/build.gradle.kts",
        root / "app/src/main/AndroidManifest.xml",
        root / "app/src/main/java/com/thanhng224/androidcomposebase/AndroidComposeBaseApplication.kt",
        root / "core/build.gradle.kts",
        root / "core/ui/build.gradle.kts",
        root / "core/api/core.api",
        root / "baselineprofile/src/main/java/com/thanhng224/androidcomposebase/baselineprofile/CriticalJourney.kt",
    ]


def validate_source(root: Path, scope: str) -> None:
    missing = [path.relative_to(root) for path in _source_markers(root) if not path.is_file()]
    if missing:
        raise InitError("Unrecognized AndroidComposeBase source; missing markers: " + ", ".join(map(str, missing)))
    settings = (root / "settings.gradle.kts").read_text(encoding="utf-8")
    app_build = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
    if f'rootProject.name = "{SOURCE_NAME}"' not in settings:
        raise InitError("This checkout is already initialized or rootProject.name was changed.")
    if f'namespace = "{SOURCE_APP_PACKAGE}"' not in app_build:
        raise InitError("This checkout is already initialized or the app namespace marker is missing.")
    if scope == "full":
        core_build = (root / "core/build.gradle.kts").read_text(encoding="utf-8")
        if f'namespace = "{SOURCE_CORE_PACKAGE}"' not in core_build:
            raise InitError("Full scope requires the original core namespace marker.")


def _replacement_pairs(project_name: str, app_package: str, core_package: str, scope: str) -> list[tuple[str, str]]:
    pairs = [
        (SOURCE_APP_PACKAGE, app_package),
        (SOURCE_APP_PACKAGE.replace(".", "/"), app_package.replace(".", "/")),
        (f"L{SOURCE_APP_PACKAGE.replace('.', '/')}/", f"L{app_package.replace('.', '/')}/"),
        ("AndroidComposeBaseApplication", f"{project_name}Application"),
        (f'rootProject.name = "{SOURCE_NAME}"', f'rootProject.name = "{project_name}"'),
        (".AndroidComposeBaseApplication", f".{project_name}Application"),
    ]
    if scope == "full":
        plugin_prefix = re.sub(r"[^a-zA-Z0-9]", "", project_name).lower()
        pairs.extend([
            (SOURCE_CORE_PACKAGE, core_package),
            (SOURCE_CORE_PACKAGE.replace(".", "/"), core_package.replace(".", "/")),
            (f"L{SOURCE_CORE_PACKAGE.replace('.', '/')}/", f"L{core_package.replace('.', '/')}/"),
            (SOURCE_PLUGIN_PACKAGE, f"{plugin_prefix}.buildlogic"),
            ("androidcomposebase.", f"{plugin_prefix}."),
            ("AndroidComposeBase", project_name),
            ("Widget.AndroidComposeBase", f"Widget.{project_name}"),
            ("TextAppearance.AndroidComposeBase", f"TextAppearance.{project_name}"),
            ('artifactId.set("AndroidComposeBase-ui")', f'artifactId.set("{project_name}-ui")'),
            ('displayName.set("AndroidComposeBase Core")', f'displayName.set("{project_name} Core")'),
            ('displayName.set("AndroidComposeBase UI")', f'displayName.set("{project_name} UI")'),
        ])
    return sorted(set(pairs), key=lambda item: len(item[0]), reverse=True)


def _package_moves(root: Path, module: Path, source_package: str, target_package: str, source_sets: Iterable[str]) -> list[tuple[Path, Path]]:
    source = Path(*source_package.split("."))
    target = Path(*target_package.split("."))
    if source == target:
        return []
    return [
        (root / module / "src" / source_set / "java" / source, root / module / "src" / source_set / "java" / target)
        for source_set in source_sets
    ]


def _planned_moves(root: Path, project_name: str, app_package: str, core_package: str, scope: str) -> list[tuple[Path, Path]]:
    old_app = root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split(".")) / "AndroidComposeBaseApplication.kt"
    new_app = root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split(".")) / f"{project_name}Application.kt"
    moves = [(old_app, new_app)]
    moves.extend(_package_moves(root, Path("app"), SOURCE_APP_PACKAGE, app_package, ("main", "test", "androidTest")))
    moves.extend(_package_moves(root, Path("baselineprofile"), f"{SOURCE_APP_PACKAGE}.baselineprofile", f"{app_package}.baselineprofile", ("main",)))
    if scope != "full":
        return moves

    moves.extend(_package_moves(root, Path("core"), SOURCE_CORE_PACKAGE, core_package, ("main", "test", "testFixtures", "androidTest")))
    moves.extend(_package_moves(root, Path("core/ui"), f"{SOURCE_CORE_PACKAGE}.ui", f"{core_package}.ui", ("main", "test", "androidTest")))

    plugin_prefix = re.sub(r"[^a-zA-Z0-9]", "", project_name).lower()
    moves.append((
        root / "build-logic/src/main/kotlin/androidcomposebase",
        root / "build-logic/src/main/kotlin" / plugin_prefix,
    ))
    for name in ("android-library", "published-library", "quality"):
        moves.append((
            root / f"build-logic/src/main/kotlin/androidcomposebase.{name}.gradle.kts",
            root / f"build-logic/src/main/kotlin/{plugin_prefix}.{name}.gradle.kts",
        ))
    return moves


def _validate_planned_moves(root: Path, moves: Iterable[tuple[Path, Path]]) -> None:
    for source, destination in moves:
        if not source.exists() or source.resolve() == destination.resolve():
            continue
        if source.is_dir() and source.resolve() in destination.resolve().parents:
            raise InitError(f"Refusing to move a directory into itself: {source.relative_to(root)} -> {destination.relative_to(root)}")
        _tracked_or_existing(root, source, destination)


def _move_source_tree(root: Path, project_name: str, app_package: str, core_package: str, scope: str, dry_run: bool) -> int:
    moves = _planned_moves(root, project_name, app_package, core_package, scope)
    _validate_planned_moves(root, moves)
    return sum(_move(root, source, destination, dry_run) for source, destination in moves)


def _replace_app_name(root: Path, app_name: str, dry_run: bool) -> int:
    count = 0
    for path in (root / "app/src/main/res/values/strings.xml", root / "app/src/main/res/values-vi/strings.xml"):
        if not path.exists():
            continue
        tree = ET.parse(path)
        node = tree.getroot().find("string[@name='app_name']")
        if node is None:
            raise InitError(f"Missing app_name resource in {path.relative_to(root)}")
        node.text = escape_android_string_resource(app_name)
        if dry_run:
            print(f"  update {path.relative_to(root)} app_name")
        else:
            tree.write(path, encoding="utf-8", xml_declaration=True)
        count += 1
    return count


def _validate_app_name_resources(root: Path) -> None:
    for path in (root / "app/src/main/res/values/strings.xml", root / "app/src/main/res/values-vi/strings.xml"):
        if not path.is_file():
            raise InitError(f"Missing app name resources: {path.relative_to(root)}")
        tree = ET.parse(path)
        if tree.getroot().find("string[@name='app_name']") is None:
            raise InitError(f"Missing app_name resource in {path.relative_to(root)}")


def _replace_text(root: Path, pairs: list[tuple[str, str]], dry_run: bool, scope: str) -> int:
    changed = 0
    ignored_files = {"init_project.py", "test_init_project.py", "smoke_init_project.py"}
    for directory, _, filenames in os.walk(root):
        for filename in filenames:
            path = Path(directory) / filename
            if filename in ignored_files or _is_ignored(path, root):
                continue
            try:
                original = path.read_text(encoding="utf-8")
            except (UnicodeDecodeError, OSError):
                continue
            updated = original
            protected_core: list[tuple[str, str]] = []
            if scope == "app-only":
                for index, marker in enumerate((SOURCE_CORE_PACKAGE, SOURCE_CORE_PACKAGE.replace(".", "/"), f"L{SOURCE_CORE_PACKAGE.replace('.', '/')}/")):
                    sentinel = f"__ANDROIDCOMPOSEBASE_CORE_{index}__"
                    if marker in updated:
                        updated = updated.replace(marker, sentinel)
                        protected_core.append((sentinel, marker))
            for source, destination in pairs:
                updated = updated.replace(source, destination)
            for sentinel, marker in protected_core:
                updated = updated.replace(sentinel, marker)
            if updated == original:
                continue
            changed += 1
            if dry_run:
                print(f"  update {path.relative_to(root)}")
            else:
                path.write_text(updated, encoding="utf-8")
    return changed


def _remove_tree(root: Path, path: Path, dry_run: bool) -> int:
    if not path.exists():
        return 0
    if dry_run:
        print(f"  remove {path.relative_to(root)}")
    elif path.is_dir():
        shutil.rmtree(path)
    else:
        path.unlink()
    return 1


def _clean_sample_route_sources(root: Path, app_package: str) -> tuple[Path, str, Path, str]:
    target_dir = root / "app/src/main/java" / Path(*app_package.split("."))
    package_dir = target_dir if target_dir.exists() else root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split("."))
    source_app_package = app_package if target_dir.exists() else SOURCE_APP_PACKAGE
    app_root = package_dir / "presentation/AppRoot.kt"
    route_file = package_dir / "navigation/ScreenRoute.kt"
    for path in (app_root, route_file):
        if not path.is_file():
            raise InitError(f"Cannot clean sample routes: missing {path.relative_to(root)}")
    source = app_root.read_text(encoding="utf-8")
    required = (
        "import androidx.compose.material.icons.filled.Cloud\n",
        "import androidx.compose.material.icons.filled.Palette\n",
        f"import {source_app_package}.sample.demo.presentation.ui.DemoScreen\n",
        f"import {source_app_package}.sample.designsystem.presentation.ui.DesignSystemScreen\n",
        "                ScreenRoute.Demo::class.qualifiedName,\n",
        "                ScreenRoute.DesignSystem::class.qualifiedName,\n",
        "                item(\n                    icon = { Icon(Icons.Default.Cloud, contentDescription = null) },\n",
        "                item(\n                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },\n",
        "                composable<ScreenRoute.Demo> {\n",
        "                composable<ScreenRoute.DesignSystem> {\n",
    )
    missing = [marker for marker in required if marker not in source]
    if missing:
        raise InitError("AppRoot sample markers changed; refusing partial cleanup: " + ", ".join(missing))
    route_markers = (
        "    @Serializable\n    public data object Demo : ScreenRoute\n",
        "    @Serializable\n    public data object DesignSystem : ScreenRoute\n",
    )
    route_source = route_file.read_text(encoding="utf-8")
    missing_routes = [marker for marker in route_markers if marker not in route_source]
    if missing_routes:
        raise InitError("ScreenRoute sample markers changed; refusing partial cleanup: " + ", ".join(missing_routes))
    source = source.replace(required[0], "").replace(required[1], "")
    source = source.replace(required[2], "").replace(required[3], "")
    for marker in required[4:6]:
        source = source.replace(marker, "")
    source = _remove_kotlin_call(source, required[6])
    source = _remove_kotlin_call(source, required[7])
    source = _remove_kotlin_block(source, required[8])
    source = _remove_kotlin_block(source, required[9])
    route_source = re.sub(r"^    @Serializable\n    public data object (?:Demo|DesignSystem) : ScreenRoute\n", "", route_source, flags=re.MULTILINE)
    if "ScreenRoute.Demo" in source or "ScreenRoute.DesignSystem" in source or "data object Demo" in route_source or "data object DesignSystem" in route_source:
        raise InitError("Sample navigation references remain after cleanup.")
    return app_root, source, route_file, route_source


def _remove_sample_routes(root: Path, app_package: str, dry_run: bool) -> int:
    app_root, source, route_file, route_source = _clean_sample_route_sources(root, app_package)
    original_source = app_root.read_text(encoding="utf-8")
    original_route_source = route_file.read_text(encoding="utf-8")
    changed = source != original_source or route_source != original_route_source
    if dry_run:
        if changed:
            print("  clean sample routes in AppRoot.kt and ScreenRoute.kt")
    else:
        app_root.write_text(source, encoding="utf-8")
        route_file.write_text(route_source, encoding="utf-8")
    return int(changed)


def _remove_delimited(source: str, marker: str, opening: str, closing: str) -> str:
    start_at = source.find(marker)
    if start_at < 0:
        raise InitError(f"Missing Kotlin sample marker: {marker.strip()}")
    delimiter_at = source.find(opening, start_at)
    if delimiter_at < 0:
        raise InitError(f"Missing Kotlin sample block delimiter after: {marker.strip()}")
    depth = 0
    in_string = False
    escaped = False
    index = delimiter_at
    while index < len(source):
        character = source[index]
        if in_string:
            if escaped:
                escaped = False
            elif character == "\\":
                escaped = True
            elif character == '"':
                in_string = False
        elif character == '"':
            in_string = True
        elif character == opening:
            depth += 1
        elif character == closing:
            depth -= 1
            if depth == 0:
                end_at = index + 1
                while end_at < len(source) and source[end_at] in " \t":
                    end_at += 1
                if end_at < len(source) and source[end_at] == "\n":
                    end_at += 1
                return source[:start_at] + source[end_at:]
        index += 1
    raise InitError(f"Unclosed Kotlin sample block: {marker.strip()}")


def _remove_kotlin_call(source: str, marker: str) -> str:
    return _remove_delimited(source, marker, "(", ")")


def _remove_kotlin_block(source: str, marker: str) -> str:
    return _remove_delimited(source, marker, "{", "}")


def _clean_sample_build(root: Path, dry_run: bool) -> int:
    build_file = root / "app/build.gradle.kts"
    source = build_file.read_text(encoding="utf-8")
    new_source = _clean_sample_build_source(source)
    changes = int(new_source != source)
    if dry_run:
        if changes:
            print("  clean app/build.gradle.kts sample dependencies/configuration")
    elif changes:
        build_file.write_text(new_source, encoding="utf-8")
    return changes


def _clean_sample_build_source(source: str) -> str:
    dependency_markers = (
        "    implementation(libs.room.runtime)\n", "    implementation(libs.room.ktx)\n", "    ksp(libs.room.compiler)\n",
        "    implementation(libs.retrofit.core)\n", "    implementation(libs.retrofit.kotlinx.serialization.converter)\n",
        "    implementation(libs.okhttp.core)\n",
        "    testImplementation(libs.okhttp.mockwebserver)\n",
        "    implementation(libs.kotlinx.serialization.json)\n",
    )
    new_source = source
    for marker in dependency_markers:
        if marker not in new_source:
            raise InitError(f"Missing sample dependency marker in app/build.gradle.kts: {marker.strip()}")
        new_source = new_source.replace(marker, "")
    new_source = re.sub(r'\s*buildConfigField\("(?:String|boolean)", "API_[A-Z_]+", [^\n]+\)', "", new_source)
    new_source = re.sub(r'\n    buildFeatures \{\n        buildConfig = true\n    }', "", new_source)
    new_source = re.sub(r'\nksp \{\n    arg\("room.schemaLocation", "\$projectDir/schemas"\)\n}', "", new_source)
    kover_marker = "\nkover {"
    if kover_marker not in new_source:
        raise InitError("Missing sample Kover configuration marker in app/build.gradle.kts")
    new_source = _remove_kotlin_block(new_source, kover_marker)
    if "Room" in new_source or "Retrofit" in new_source or "API_BASE_URL" in new_source or "sample.demo" in new_source or "sample.designsystem" in new_source:
        raise InitError("Sample-only build configuration remains after cleanup.")
    return new_source


def _clean_sample_resources(root: Path, dry_run: bool) -> int:
    changed = 0
    for resource_file in [root / "app/src/main/res/values/strings.xml", root / "app/src/main/res/values-vi/strings.xml"]:
        if not resource_file.exists():
            continue
        tree = ET.parse(resource_file)
        parent = tree.getroot()
        sample_names = {
            "navigation_demo", "navigation_design", "appshell_home_greeting", "appshell_home_subtitle",
            "home_title", "home_card_eyebrow", "home_card_title", "home_card_body", "home_navigation_hint",
            "error_network", "error_parse", "error_empty_body",
        }
        to_remove = [node for node in list(parent) if node.attrib.get("name", "").startswith(("demo_", "design_system_")) or node.attrib.get("name") in sample_names]
        if to_remove:
            changed += len(to_remove)
            for node in to_remove:
                parent.remove(node)
            if dry_run:
                print(f"  clean {resource_file.relative_to(root)} ({len(to_remove)} strings)")
            else:
                tree.write(resource_file, encoding="utf-8", xml_declaration=True)
    return changed


def _metadata_logger_path(root: Path, app_package: str) -> Path:
    return root / "app/src/main/java" / Path(*app_package.split(".")) / "di/MetadataLoggingInterceptor.kt"


def _validate_metadata_logger(root: Path, app_package: str) -> Path:
    path = _metadata_logger_path(root, app_package)
    if not path.is_file():
        raise InitError(f"Missing sample metadata logging interceptor: {path.relative_to(root)}")
    try:
        source = path.read_text(encoding="utf-8")
    except (UnicodeDecodeError, OSError) as error:
        raise InitError(f"Cannot read sample metadata logging interceptor: {path.relative_to(root)}") from error
    required = (f"package {app_package}.di\n", *METADATA_LOGGER_MARKERS)
    missing = [marker for marker in required if marker not in source]
    if missing:
        raise InitError(f"Unrecognized sample metadata logging interceptor: {path.relative_to(root)}")
    return path


def _remove_metadata_logger(root: Path, app_package: str, dry_run: bool) -> int:
    path = _validate_metadata_logger(root, app_package)
    return _remove_tree(root, path, dry_run)


def clean_samples(root: Path, app_package: str, dry_run: bool) -> int:
    changed = _remove_sample_routes(root, app_package, dry_run)
    target_dir = root / "app/src/main/java" / Path(*app_package.split("."))
    package_dir = target_dir if target_dir.exists() else root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split("."))
    logger_package = app_package if target_dir.exists() else SOURCE_APP_PACKAGE
    for sample_dir in (package_dir / "sample/demo", package_dir / "sample/designsystem"):
        changed += _remove_tree(root, sample_dir, dry_run)
    test_package_dir = root / "app/src/test/java" / Path(*app_package.split(".")) / "sample"
    for sample_dir in (test_package_dir / "demo", test_package_dir / "designsystem"):
        changed += _remove_tree(root, sample_dir, dry_run)
    changed += _remove_tree(root, package_dir / "di/AppNetworkModule.kt", dry_run)
    changed += _remove_metadata_logger(root, logger_package, dry_run)
    changed += _remove_tree(root, root / "app/src/main/res/drawable/ic_nav_demo.xml", dry_run)
    changed += _remove_tree(root, root / "app/src/main/res/drawable/ic_nav_ui_kit.xml", dry_run)
    changed += _remove_tree(root, root / "app/schemas", dry_run)
    journey_package = app_package if target_dir.exists() else SOURCE_APP_PACKAGE
    changed += _clean_critical_journey(root, app_package, dry_run, journey_source_package=journey_package)
    changed += _clean_sample_resources(root, dry_run)
    changed += _clean_sample_build(root, dry_run)

    manifest_path = root / "app/src/main/AndroidManifest.xml"
    manifest = manifest_path.read_text(encoding="utf-8")
    if '    <uses-permission android:name="android.permission.INTERNET" />\n' not in manifest:
        raise InitError("Missing Internet permission marker in AndroidManifest.xml")
    manifest = manifest.replace('    <uses-permission android:name="android.permission.INTERNET" />\n', "")
    if dry_run:
        print("  remove sample-only Internet permission")
    else:
        manifest_path.write_text(manifest, encoding="utf-8")
    changed += 1

    if changed == 0:
        raise InitError("--clean-samples found no known sample markers; refusing to report a no-op success.")
    return changed


def validate_clean_sample_source(root: Path) -> None:
    """Validate every marker cleanup needs while the checkout is still untouched."""
    _clean_sample_route_sources(root, SOURCE_APP_PACKAGE)
    _validate_metadata_logger(root, SOURCE_APP_PACKAGE)
    build_file = root / "app/build.gradle.kts"
    _clean_sample_build_source(build_file.read_text(encoding="utf-8"))
    for resource_file in (root / "app/src/main/res/values/strings.xml", root / "app/src/main/res/values-vi/strings.xml"):
        if not resource_file.is_file():
            raise InitError(f"Missing sample string resources: {resource_file.relative_to(root)}")
        ET.parse(resource_file)
    manifest_path = root / "app/src/main/AndroidManifest.xml"
    manifest = manifest_path.read_text(encoding="utf-8")
    if '    <uses-permission android:name="android.permission.INTERNET" />\n' not in manifest:
        raise InitError("Missing Internet permission marker in AndroidManifest.xml")
    journey_path = root / "baselineprofile/src/main/java" / Path(*f"{SOURCE_APP_PACKAGE}.baselineprofile".split(".")) / "CriticalJourney.kt"
    if not journey_path.is_file():
        raise InitError(f"Missing baseline profile journey: {journey_path.relative_to(root)}")


def _clean_critical_journey(root: Path, app_package: str, dry_run: bool, journey_source_package: str | None = None) -> int:
    source_package = journey_source_package or app_package
    path = root / "baselineprofile/src/main/java" / Path(*source_package.split(".")) / "baselineprofile/CriticalJourney.kt"
    if not path.is_file():
        raise InitError(f"Missing baseline profile journey: {path.relative_to(root)}")
    replacement = f'''package {app_package}.baselineprofile

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

private const val WAIT_TIMEOUT_MS = 15_000L

internal object CriticalJourney {{
    private fun textSelector(vararg labels: String): BySelector {{
        val regex = labels.joinToString("|") {{ Pattern.quote(it) }}
        return By.text(Pattern.compile(regex))
    }}

    private fun UiDevice.clickOrFail(selector: BySelector, what: String) {{
        checkNotNull(findObject(selector)) {{ "Could not find $what" }}.click()
    }}

    fun execute(device: UiDevice, packageName: String) {{
        device.findObject(textSelector("Get Started", "Bắt đầu"))?.click()
        check(device.wait(Until.hasObject(textSelector("Home", "Trang chủ")), WAIT_TIMEOUT_MS)) {{ "Home did not appear" }}
        device.clickOrFail(textSelector("Settings", "Cài đặt"), "the Settings tab")
        check(device.wait(Until.hasObject(textSelector("Appearance", "Giao diện")), WAIT_TIMEOUT_MS)) {{ "Settings did not appear" }}
        device.clickOrFail(textSelector("Home", "Trang chủ"), "the Home tab")
        check(device.wait(Until.hasObject(textSelector("Home", "Trang chủ")), WAIT_TIMEOUT_MS)) {{ "Home did not reappear" }}
    }}
}}
'''
    existing = path.read_text(encoding="utf-8")
    if existing == replacement:
        return 0
    if dry_run:
        print(f"  update {path.relative_to(root)} for the retained app journey")
    else:
        path.write_text(replacement, encoding="utf-8")
    return 1


def _verify(root: Path, app_package: str, core_package: str, scope: str, clean: bool) -> None:
    app_build = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
    if f'namespace = "{app_package}"' not in app_build or f'applicationId = "{app_package}"' not in app_build:
        raise InitError("Post-check failed: app namespace/applicationId do not match the requested package.")
    if scope == "full":
        core_build = (root / "core/build.gradle.kts").read_text(encoding="utf-8")
        ui_build = (root / "core/ui/build.gradle.kts").read_text(encoding="utf-8")
        if f'namespace = "{core_package}"' not in core_build or f'namespace = "{core_package}.ui"' not in ui_build:
            raise InitError("Post-check failed: core or core:ui namespace did not update.")
    for marker in ("AndroidComposeBaseApplication",):
        if _find_marker(root, marker):
            raise InitError(f"Post-check failed: source marker remains: {marker}")
    if scope == "full":
        for marker in (SOURCE_APP_PACKAGE, SOURCE_CORE_PACKAGE, SOURCE_PLUGIN_PACKAGE):
            if _find_marker(root, marker):
                raise InitError(f"Post-check failed: full-scope marker remains: {marker}")
    else:
        old_app_dir = root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split("."))
        old_profile_dir = root / "baselineprofile/src/main/java" / Path(*f"{SOURCE_APP_PACKAGE}.baselineprofile".split("."))
        if old_app_dir.exists() or old_profile_dir.exists():
            raise InitError("Post-check failed: app-only source package path remains.")
    stale_profile = root / "app/src/release/generated/baselineProfiles/baseline-prof.txt"
    if stale_profile.exists():
        raise InitError("Post-check failed: committed baseline profile must be regenerated for the renamed app.")
    if clean:
        package_dir = root / "app/src/main/java" / Path(*app_package.split("."))
        test_package_dir = root / "app/src/test/java" / Path(*app_package.split(".")) / "sample"
        markers = (
            package_dir / "sample/demo", package_dir / "sample/designsystem", package_dir / "di/AppNetworkModule.kt",
            package_dir / "di/MetadataLoggingInterceptor.kt",
            test_package_dir / "demo", test_package_dir / "designsystem",
            root / "app/src/release/generated/baselineProfiles/baseline-prof.txt",
        )
        leftovers = [path.relative_to(root) for path in markers if path.exists()]
        app_root = (package_dir / "presentation/AppRoot.kt").read_text(encoding="utf-8")
        routes = (package_dir / "navigation/ScreenRoute.kt").read_text(encoding="utf-8")
        app_build = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
        manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        if "ScreenRoute.Demo" in app_root or "ScreenRoute.DesignSystem" in app_root:
            leftovers.append(Path("app/src/main/java") / Path(*app_package.split(".")) / "presentation/AppRoot.kt")
        if "data object Demo" in routes or "data object DesignSystem" in routes:
            leftovers.append(Path("app/src/main/java") / Path(*app_package.split(".")) / "navigation/ScreenRoute.kt")
        if any(marker in app_build for marker in ("libs.room.", "libs.retrofit.", "libs.okhttp.", "API_BASE_URL", "sample.demo", "sample.designsystem")):
            leftovers.append(Path("app/build.gradle.kts"))
        if "android.permission.INTERNET" in manifest:
            leftovers.append(Path("app/src/main/AndroidManifest.xml"))
        sample_resource_names = {
            "navigation_demo", "navigation_design", "appshell_home_greeting", "appshell_home_subtitle",
            "home_title", "home_card_eyebrow", "home_card_title", "home_card_body", "home_navigation_hint",
            "error_network", "error_parse", "error_empty_body",
        }
        for relative in ("app/src/main/res/values/strings.xml", "app/src/main/res/values-vi/strings.xml"):
            resources = ET.parse(root / relative).getroot()
            stale_strings = [node.attrib.get("name", "") for node in resources if node.attrib.get("name", "").startswith(("demo_", "design_system_")) or node.attrib.get("name") in sample_resource_names]
            if stale_strings:
                leftovers.append(Path(relative))
        if leftovers:
            raise InitError("Post-check failed: cleaned sample remnants: " + ", ".join(map(str, leftovers)))


def _find_marker(root: Path, marker: str) -> bool:
    for directory, _, filenames in os.walk(root):
        for filename in filenames:
            path = Path(directory) / filename
            if filename in {"init_project.py", "test_init_project.py", "smoke_init_project.py"} or _is_ignored(path, root):
                continue
            try:
                if marker in path.read_text(encoding="utf-8"):
                    return True
            except (UnicodeDecodeError, OSError):
                continue
    return False


def _rename_plugin_files(root: Path, project_name: str, dry_run: bool) -> int:
    count = 0
    prefix = re.sub(r"[^a-zA-Z0-9]", "", project_name).lower()
    for name in ("android-library", "published-library", "quality"):
        source = root / f"build-logic/src/main/kotlin/androidcomposebase.{name}.gradle.kts"
        destination = root / f"build-logic/src/main/kotlin/{prefix}.{name}.gradle.kts"
        count += _move(root, source, destination, dry_run)
    return count


def run(root: Path, project_name: str, app_name: str, app_package: str, core_package: str, scope: str, clean: bool, dry_run: bool, force: bool, skip_build_check: bool) -> int:
    if scope not in {"full", "app-only"}:
        raise InitError("Scope must be 'full' or 'app-only'.")
    if not valid_project_name(project_name):
        raise InitError("Project name must be PascalCase and contain only letters, digits, and underscores.")
    if not valid_package(app_package) or not valid_package(core_package):
        raise InitError("Application and core packages must be valid lowercase Kotlin package names.")
    if not app_name.strip():
        raise InitError("App display name must not be empty.")
    if scope == "full" and app_package == core_package:
        raise InitError("Application and core packages must be different.")
    validate_source(root, scope)
    _validate_app_name_resources(root)
    if clean:
        validate_clean_sample_source(root)
    if not dry_run and not force and (root / ".git").exists():
        status = subprocess.run(["git", "status", "--porcelain"], cwd=root, check=True, capture_output=True, text=True)
        if status.stdout.strip():
            raise InitError("Git working tree has changes. Commit/stash first or pass --force.")

    moves = _move_source_tree(root, project_name, app_package, core_package, scope, dry_run)
    changes = _replace_text(root, _replacement_pairs(project_name, app_package, core_package, scope), dry_run, scope)
    changes += _replace_app_name(root, app_name, dry_run)
    changes += _remove_tree(root, root / "app/src/release/generated/baselineProfiles/baseline-prof.txt", dry_run)
    if moves + changes == 0:
        raise InitError("No source markers were changed; refusing to report initialization success.")
    if clean:
        clean_samples(root, app_package, dry_run)
    if dry_run:
        print("Dry run complete; no files were changed.")
        return moves + changes

    _verify(root, app_package, core_package, scope, clean)
    if not skip_build_check:
        gradlew = root / ("gradlew.bat" if os.name == "nt" else "gradlew")
        if not gradlew.exists():
            raise InitError("Gradle wrapper is missing; use --skip-build-check only if you will build separately.")
        tasks = [":app:assembleDebug"]
        if scope == "full":
            tasks.extend([":core:apiDump", ":core:ui:apiDump"])
        subprocess.run([str(gradlew), *tasks], cwd=root, check=True)
        subprocess.run([str(gradlew), "check"], cwd=root, check=True)
    return moves + changes


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Initialize AndroidComposeBase for a new application.")
    parser.add_argument("--project-name", required=True, help="PascalCase project name, for example AcmeShop")
    parser.add_argument("--app-name", help="Human-readable launcher name")
    parser.add_argument("--package", required=True, dest="app_package", help="Application package / application ID")
    parser.add_argument("--core-package", help="Core package in full scope (defaults to <package>.core)")
    parser.add_argument("--scope", choices=("full", "app-only"), default="full")
    parser.add_argument("--clean-samples", action="store_true", help="Remove the weather and design-system demos")
    parser.add_argument("--dry-run", action="store_true", help="Print planned changes without writing")
    parser.add_argument("--skip-build-check", action="store_true", help="Skip builds after the rename")
    parser.add_argument("--force", action="store_true", help="Allow initializing a dirty Git checkout")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    root = Path(__file__).resolve().parent.parent
    core_package = args.core_package or (f"{args.app_package}.core" if args.scope == "full" else SOURCE_CORE_PACKAGE)
    app_name = args.app_name or re.sub(r"([a-z])([A-Z])", r"\1 \2", args.project_name)
    try:
        changed = run(root, args.project_name, app_name, args.app_package, core_package, args.scope, args.clean_samples, args.dry_run, args.force, args.skip_build_check)
    except (InitError, OSError, subprocess.CalledProcessError, ET.ParseError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 1
    print(f"Initialized {args.project_name} ({args.scope}; clean_samples={args.clean_samples}) with {changed} source changes.")
    if not args.dry_run:
        print("Generate a fresh baseline profile after adding your app journeys: ./gradlew :app:generateBaselineProfile")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
