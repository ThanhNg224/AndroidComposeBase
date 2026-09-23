#!/usr/bin/env python3
"""Fast focused tests for the project initializer; full source copies live in smoke_init_project.py."""

from __future__ import annotations

import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

from init_project import (
    SOURCE_APP_PACKAGE,
    SOURCE_CORE_PACKAGE,
    SOURCE_NAME,
    InitError,
    kotlin_string_literal,
    _replacement_pairs,
    _replace_app_branding,
    _replace_app_name,
    _move,
    escape_android_string_resource,
    valid_package,
    valid_project_name,
    run,
    validate_source,
)


class InitializerUnitTests(unittest.TestCase):
    @staticmethod
    def write_preflight_fixture(root: Path) -> None:
        app_dir = root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split("."))
        profile_dir = root / "baselineprofile/src/main/java" / Path(*f"{SOURCE_APP_PACKAGE}.baselineprofile".split("."))
        app_root = app_dir / "presentation/AppRoot.kt"
        app_root.parent.mkdir(parents=True)
        app_root.write_text(
            f'''package {SOURCE_APP_PACKAGE}.presentation
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Palette
import {SOURCE_APP_PACKAGE}.sample.demo.presentation.ui.DemoScreen
import {SOURCE_APP_PACKAGE}.sample.designsystem.presentation.ui.DesignSystemScreen
val topRoutes = listOf(
                ScreenRoute.Demo::class.qualifiedName,
                ScreenRoute.DesignSystem::class.qualifiedName,
)
                    composable<ScreenRoute.Demo> {{
                        DemoScreen()
                    }}
                    composable<ScreenRoute.Settings> {{
                        SettingsScreen()
                    }}
                    composable<ScreenRoute.DesignSystem> {{
                        DesignSystemScreen()
                    }}
                }}

                LaunchedEffect(value) {{ }}
                            NavItem(
                                title = "Home",
                            ),
                            NavItem(
                                title = "Demo",
                            ),
                            NavItem(
                                title = "Design",
                            ),
                            NavItem(
                                title = "Settings",
                            ),
''',
            encoding="utf-8",
        )
        app_source = app_dir / "AndroidComposeBaseApplication.kt"
        app_source.write_text(f"package {SOURCE_APP_PACKAGE}\nclass AndroidComposeBaseApplication\n", encoding="utf-8")
        onboarding = app_dir / "feature/onboarding/presentation/ui/OnboardingScreen.kt"
        onboarding.parent.mkdir(parents=True)
        onboarding.write_text('Text(text = "AndroidComposeBase")\n', encoding="utf-8")
        home = app_dir / "appshell/home/HomeScreen.kt"
        home.parent.mkdir(parents=True)
        home.write_text('AppCenterTopBar(title = "AndroidComposeBase")\n', encoding="utf-8")
        routes = app_dir / "navigation/ScreenRoute.kt"
        routes.parent.mkdir(parents=True)
        routes.write_text(
            "\n    @Serializable\n    public data object Demo : ScreenRoute\n"
            "\n    @Serializable\n    public data object DesignSystem : ScreenRoute\n",
            encoding="utf-8",
        )
        for sample in ("demo", "designsystem"):
            sample_file = app_dir / f"sample/{sample}/Sample.kt"
            sample_file.parent.mkdir(parents=True)
            sample_file.write_text("sample\n", encoding="utf-8")
        di = app_dir / "di/AppNetworkModule.kt"
        di.parent.mkdir(parents=True)
        di.write_text("network sample\n", encoding="utf-8")

        profile = profile_dir / "CriticalJourney.kt"
        profile.parent.mkdir(parents=True)
        profile.write_text(f"package {SOURCE_APP_PACKAGE}.baselineprofile\n", encoding="utf-8")
        (root / "settings.gradle.kts").write_text(f'rootProject.name = "{SOURCE_NAME}"\n', encoding="utf-8")
        (root / "app/build.gradle.kts").write_text(
            f'''android {{
    namespace = "{SOURCE_APP_PACKAGE}"
    defaultConfig {{ applicationId = "{SOURCE_APP_PACKAGE}" }}
}}
dependencies {{
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    testImplementation(libs.okhttp.mockwebserver)
}}
ksp {{
    arg("room.schemaLocation", "$projectDir/schemas")
}}
''',
            encoding="utf-8",
        )
        for relative in ("app/src/main/res/values/strings.xml", "app/src/main/res/values-vi/strings.xml"):
            path = root / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text('<resources><string name="app_name">Starter</string></resources>', encoding="utf-8")
        manifest = root / "app/src/main/AndroidManifest.xml"
        manifest.parent.mkdir(parents=True, exist_ok=True)
        manifest.write_text('<manifest />\n', encoding="utf-8")
        (root / "core/build.gradle.kts").parent.mkdir(parents=True, exist_ok=True)
        (root / "core/build.gradle.kts").write_text(f'namespace = "{SOURCE_CORE_PACKAGE}"\n', encoding="utf-8")
        (root / "core/ui/build.gradle.kts").parent.mkdir(parents=True, exist_ok=True)
        (root / "core/ui/build.gradle.kts").write_text(f'namespace = "{SOURCE_CORE_PACKAGE}.ui"\n', encoding="utf-8")
        (root / "core/api/core.api").parent.mkdir(parents=True, exist_ok=True)
        (root / "core/api/core.api").write_text("api snapshot\n", encoding="utf-8")

    def test_clean_sample_marker_failure_does_not_partially_rename_checkout(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_preflight_fixture(root)
            before = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}
            with self.assertRaisesRegex(InitError, "Internet permission marker"):
                run(
                    root=root,
                    project_name="AcmeShop",
                    app_name="Acme Shop",
                    app_package="com.acme.shop",
                    core_package="com.acme.shop.core",
                    scope="app-only",
                    clean=True,
                    dry_run=False,
                    force=True,
                    skip_build_check=True,
                )
            after = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}
            self.assertEqual(after, before)

    def test_late_core_destination_collision_does_not_partially_move_checkout(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_preflight_fixture(root)
            core_source = root / "core/src/main/java" / Path(*SOURCE_CORE_PACKAGE.split(".")) / "Marker.kt"
            core_source.parent.mkdir(parents=True)
            core_source.write_text("package marker\n", encoding="utf-8")
            core_collision = root / "core/src/main/java/com/acme/shop/core/Keep.kt"
            core_collision.parent.mkdir(parents=True)
            core_collision.write_text("preserve me\n", encoding="utf-8")
            before = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}
            with self.assertRaisesRegex(InitError, "Refusing to overwrite existing path"):
                run(
                    root=root,
                    project_name="AcmeShop",
                    app_name="Acme Shop",
                    app_package="com.acme.shop",
                    core_package="com.acme.shop.core",
                    scope="full",
                    clean=False,
                    dry_run=False,
                    force=True,
                    skip_build_check=True,
                )
            after = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}
            self.assertEqual(after, before)

    def test_package_target_nested_in_source_is_rejected_before_any_move(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_preflight_fixture(root)
            before = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}
            with self.assertRaisesRegex(InitError, "move a directory into itself"):
                run(
                    root=root,
                    project_name="AcmeShop",
                    app_name="Acme Shop",
                    app_package=f"{SOURCE_APP_PACKAGE}.feature",
                    core_package=SOURCE_CORE_PACKAGE,
                    scope="app-only",
                    clean=False,
                    dry_run=False,
                    force=True,
                    skip_build_check=True,
                )
            after = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}
            self.assertEqual(after, before)

    def test_validates_names_and_packages(self) -> None:
        self.assertTrue(valid_project_name("AcmeShop_2"))
        self.assertFalse(valid_project_name("acmeShop"))
        self.assertTrue(valid_package("com.acme.shop"))
        self.assertFalse(valid_package("com.when.shop"))
        self.assertFalse(valid_package("com.acme..shop"))

    def test_android_string_escaping_handles_resource_prefix_and_quotes(self) -> None:
        self.assertEqual(escape_android_string_resource("@New's \"Shop\""), "\\@New\\'s \\\"Shop\\\"")

    def test_kotlin_display_literal_escapes_interpolation_and_quotes(self) -> None:
        self.assertEqual(kotlin_string_literal('Shop "$price"\n'), '"Shop \\"\\$price\\"\\n"')

    def test_app_branding_replacement_is_scoped_to_app_screens(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            app_dir = root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split("."))
            onboarding = app_dir / "feature/onboarding/presentation/ui/OnboardingScreen.kt"
            home = app_dir / "appshell/home/HomeScreen.kt"
            unrelated = app_dir / "core/ui/theme/Theme.kt"
            for path in (onboarding, home, unrelated):
                path.parent.mkdir(parents=True, exist_ok=True)
            onboarding.write_text('Text(text = "AndroidComposeBase")\n', encoding="utf-8")
            home.write_text('AppCenterTopBar(title = "AndroidComposeBase")\n', encoding="utf-8")
            unrelated.write_text('val theme = AndroidComposeBaseTheme\n', encoding="utf-8")
            self.assertEqual(_replace_app_branding(root, "AcmeShop", SOURCE_APP_PACKAGE, "AndroidComposeBase $Shop", "app-only", False), 2)
            self.assertIn('"AndroidComposeBase \\$Shop"', onboarding.read_text(encoding="utf-8"))
            self.assertIn('"AndroidComposeBase \\$Shop"', home.read_text(encoding="utf-8"))
            self.assertEqual(unrelated.read_text(encoding="utf-8"), "val theme = AndroidComposeBaseTheme\n")

    def test_full_replacements_cover_current_compose_library_identity(self) -> None:
        pairs = dict(_replacement_pairs("AcmeShop", "com.acme.app", "com.acme.core", "full"))
        self.assertEqual(pairs[SOURCE_APP_PACKAGE], "com.acme.app")
        self.assertEqual(pairs[SOURCE_CORE_PACKAGE], "com.acme.core")
        self.assertEqual(pairs["androidcomposebase."], "acmeshop.")
        self.assertEqual(pairs["AndroidComposeBase"], "AcmeShop")

    def test_app_only_replacements_preserve_library_contract(self) -> None:
        pairs = dict(_replacement_pairs("AcmeShop", "com.acme.app", SOURCE_CORE_PACKAGE, "app-only"))
        self.assertIn(SOURCE_APP_PACKAGE, pairs)
        self.assertNotIn(SOURCE_CORE_PACKAGE, pairs)
        self.assertNotIn("AndroidComposeBase", pairs)

    def test_source_validation_rejects_missing_or_initialized_markers(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            with self.assertRaisesRegex(InitError, "missing markers"):
                validate_source(root, "full")

            for relative in (
                "settings.gradle.kts", "app/build.gradle.kts", "app/src/main/AndroidManifest.xml",
                "app/src/main/java/com/thanhng224/androidcomposebase/AndroidComposeBaseApplication.kt",
                "core/build.gradle.kts", "core/ui/build.gradle.kts", "core/api/core.api",
                "baselineprofile/src/main/java/com/thanhng224/androidcomposebase/baselineprofile/CriticalJourney.kt",
            ):
                path = root / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.touch()
            (root / "settings.gradle.kts").write_text('rootProject.name = "AcmeShop"\n', encoding="utf-8")
            (root / "app/build.gradle.kts").write_text('namespace = "com.acme.shop"\n', encoding="utf-8")
            (root / "core/build.gradle.kts").write_text(f'namespace = "{SOURCE_CORE_PACKAGE}"\n', encoding="utf-8")
            with self.assertRaisesRegex(InitError, "already initialized"):
                validate_source(root, "full")

    def test_move_never_overwrites_destination(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source, destination = root / "old.txt", root / "new.txt"
            source.write_text("source", encoding="utf-8")
            destination.write_text("keep", encoding="utf-8")
            with self.assertRaisesRegex(InitError, "Refusing to overwrite"):
                _move(root, source, destination, dry_run=False)
            self.assertEqual(destination.read_text(encoding="utf-8"), "keep")

    def test_display_name_update_keeps_xml_parseable(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            for relative in ("app/src/main/res/values/strings.xml", "app/src/main/res/values-vi/strings.xml"):
                path = root / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text('<resources><string name="app_name">Starter</string></resources>', encoding="utf-8")
            self.assertEqual(_replace_app_name(root, 'R&D "Mobile"', dry_run=False), 2)
            for relative in ("app/src/main/res/values/strings.xml", "app/src/main/res/values-vi/strings.xml"):
                root_node = ET.parse(root / relative).getroot()
                self.assertEqual(root_node.find("string[@name='app_name']").text, 'R&D \\"Mobile\\"')


if __name__ == "__main__":
    unittest.main(verbosity=2)
