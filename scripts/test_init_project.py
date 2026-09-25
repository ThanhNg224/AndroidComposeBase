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
    _replacement_pairs,
    _replace_app_name,
    _clean_critical_journey,
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
import {SOURCE_APP_PACKAGE}.sample.demo.presentation.ui.DemoScreen
import {SOURCE_APP_PACKAGE}.sample.designsystem.presentation.ui.DesignSystemScreen
val topLevelRoutes =
            listOf(
                ScreenRoute.Home::class.qualifiedName,
                ScreenRoute.Demo::class.qualifiedName,
                ScreenRoute.Settings::class.qualifiedName,
                ScreenRoute.DesignSystem::class.qualifiedName,
            )
NavHost(navController, startDestination = ScreenRoute.Home) {{
                composable<ScreenRoute.Home> {{
                    HomeScreen()
                }}
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
val navItems =
                    listOf(
                        AppNavItem(
                            selected = isSelectedRoute(currentDestination, ScreenRoute.Home::class.qualifiedName),
                            onClick = {{ navController.navigate(ScreenRoute.Home) }},
                            label = stringResource(R.string.navigation_home),
                        ),
                        AppNavItem(
                            selected = isSelectedRoute(currentDestination, ScreenRoute.Demo::class.qualifiedName),
                            onClick = {{ navController.navigate(ScreenRoute.Demo) }},
                            label = stringResource(R.string.navigation_demo),
                        ),
                        AppNavItem(
                            selected = isSelectedRoute(currentDestination, ScreenRoute.DesignSystem::class.qualifiedName),
                            onClick = {{ navController.navigate(ScreenRoute.DesignSystem) }},
                            label = stringResource(R.string.navigation_design),
                        ),
                        AppNavItem(
                            selected = isSelectedRoute(currentDestination, ScreenRoute.Settings::class.qualifiedName),
                            onClick = {{ navController.navigate(ScreenRoute.Settings) }},
                            label = stringResource(R.string.navigation_settings),
                        ),
                    )
AppFloatingNavBar(items = navItems)
''',
            encoding="utf-8",
        )
        app_source = app_dir / "AndroidComposeBaseApplication.kt"
        app_source.write_text(f"package {SOURCE_APP_PACKAGE}\nclass AndroidComposeBaseApplication\n", encoding="utf-8")
        onboarding = app_dir / "feature/onboarding/presentation/ui/OnboardingScreen.kt"
        onboarding.parent.mkdir(parents=True)
        onboarding.write_text('Text(text = stringResource(R.string.onboarding_title))\n', encoding="utf-8")
        home = app_dir / "appshell/home/HomeScreen.kt"
        home.parent.mkdir(parents=True)
        home.write_text('Text(text = stringResource(R.string.home_welcome_title))\n', encoding="utf-8")
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
            sample_test = root / "app/src/test/java" / Path(*SOURCE_APP_PACKAGE.split(".")) / f"sample/{sample}/SampleTest.kt"
            sample_test.parent.mkdir(parents=True)
            sample_test.write_text("sample test\n", encoding="utf-8")
        di = app_dir / "di/AppNetworkModule.kt"
        di.parent.mkdir(parents=True)
        di.write_text("network sample\n", encoding="utf-8")
        (di.parent / "MetadataLoggingInterceptor.kt").write_text(
            f'''package {SOURCE_APP_PACKAGE}.di
import android.util.Log
internal class MetadataLoggingInterceptor(
    private companion object {{
        const val TAG = "NetworkMetadata"
        val log = {{ Log.d(TAG, it) }}
    }}
}}
''',
            encoding="utf-8",
        )

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
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.okhttp.mockwebserver)
}}
ksp {{
    arg("room.schemaLocation", "$projectDir/schemas")
}}
kover {{
    reports {{
        filters {{
            includes {{
                classes(
                    "*.startup.*",
                    "*.sample.demo.*",
                )
            }}
            excludes {{ classes("*.BuildConfig") }}
        }}
        verify {{
            rule {{
                minBound(80)
            }}
        }}
    }}
}}
''',
            encoding="utf-8",
        )
        for relative in ("app/src/main/res/values/strings.xml", "app/src/main/res/values-vi/strings.xml"):
            path = root / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(
                '<resources><string name="app_name">Starter</string><string name="navigation_demo">Demo</string><string name="navigation_design">Design</string><string name="demo_title">Demo</string><string name="design_system_title">UI Kit</string></resources>',
                encoding="utf-8",
            )
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

    def test_clean_samples_removes_metadata_logger_for_full_and_app_only_scope(self) -> None:
        for scope in ("full", "app-only"):
            with self.subTest(scope=scope), tempfile.TemporaryDirectory() as temp_dir:
                root = Path(temp_dir)
                self.write_preflight_fixture(root)
                manifest = root / "app/src/main/AndroidManifest.xml"
                manifest.write_text(
                    '<manifest>\n    <uses-permission android:name="android.permission.INTERNET" />\n</manifest>\n',
                    encoding="utf-8",
                )
                before_dry_run = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}

                run(
                    root=root,
                    project_name="AcmeShop",
                    app_name="Acme Shop",
                    app_package="com.acme.shop",
                    core_package="com.acme.shop.core",
                    scope=scope,
                    clean=True,
                    dry_run=True,
                    force=True,
                    skip_build_check=True,
                )

                after_dry_run = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}
                self.assertEqual(after_dry_run, before_dry_run)

                run(
                    root=root,
                    project_name="AcmeShop",
                    app_name="Acme Shop",
                    app_package="com.acme.shop",
                    core_package="com.acme.shop.core",
                    scope=scope,
                    clean=True,
                    dry_run=False,
                    force=True,
                    skip_build_check=True,
                )

                self.assertFalse(root.joinpath("app/src/main/java/com/acme/shop/di/MetadataLoggingInterceptor.kt").exists())
                self.assertFalse(root.joinpath("app/src/test/java/com/acme/shop/sample/demo").exists())
                self.assertFalse(root.joinpath("app/src/test/java/com/acme/shop/sample/designsystem").exists())
                for relative in ("app/src/main/res/values/strings.xml", "app/src/main/res/values-vi/strings.xml"):
                    names = {node.attrib.get("name") for node in ET.parse(root / relative).getroot()}
                    self.assertTrue(names.isdisjoint({"navigation_demo", "navigation_design", "demo_title", "design_system_title"}))

                cleaned_build = root.joinpath("app/build.gradle.kts").read_text(encoding="utf-8")
                self.assertIn("kover {", cleaned_build)
                self.assertIn('"*.startup.*"', cleaned_build)
                self.assertIn("minBound(80)", cleaned_build)
                self.assertNotIn("sample.demo", cleaned_build)

    def test_clean_samples_keeps_kover_coverage_gate_and_only_drops_sample_includes(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_preflight_fixture(root)
            manifest = root / "app/src/main/AndroidManifest.xml"
            manifest.write_text(
                '<manifest>\n    <uses-permission android:name="android.permission.INTERNET" />\n</manifest>\n',
                encoding="utf-8",
            )

            from init_project import _clean_sample_build_source

            cleaned = _clean_sample_build_source((root / "app/build.gradle.kts").read_text(encoding="utf-8"))

            self.assertIn("kover {", cleaned)
            self.assertIn('"*.startup.*"', cleaned)
            self.assertIn("verify {", cleaned)
            self.assertIn("minBound(80)", cleaned)
            self.assertNotIn("sample.demo", cleaned)
            self.assertNotIn("sample.designsystem", cleaned)

    def test_unrecognized_metadata_logger_fails_before_renaming_checkout(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_preflight_fixture(root)
            logger = root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split(".")) / "di/MetadataLoggingInterceptor.kt"
            logger.write_text("package custom.networking\nclass CustomLogger\n", encoding="utf-8")
            before = {path.relative_to(root): path.read_bytes() for path in root.rglob("*") if path.is_file()}

            with self.assertRaisesRegex(InitError, "Unrecognized sample metadata logging interceptor"):
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

    def test_app_display_name_is_updated_in_both_locale_resources(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            resource_files = (
                root / "app/src/main/res/values/strings.xml",
                root / "app/src/main/res/values-vi/strings.xml",
            )
            for path in resource_files:
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text('<resources><string name="app_name">Android Compose Base</string></resources>', encoding="utf-8")
            self.assertEqual(_replace_app_name(root, '@Acme\'s "Shop"', dry_run=False), 2)
            for path in resource_files:
                app_name = ET.parse(path).getroot().find("string[@name='app_name']")
                self.assertEqual(app_name.text, "\\@Acme\\'s \\\"Shop\\\"")

    def test_clean_routes_rejects_unknown_adaptive_navigation_markers(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_preflight_fixture(root)
            app_root = root / "app/src/main/java" / Path(*SOURCE_APP_PACKAGE.split(".")) / "presentation/AppRoot.kt"
            app_root.write_text(
                app_root.read_text(encoding="utf-8").replace(
                    "selected = isSelectedRoute(currentDestination, ScreenRoute.Demo::class.qualifiedName),",
                    "selected = isSelectedRoute(currentDestination, ScreenRoute.Weather::class.qualifiedName),",
                ),
                encoding="utf-8",
            )
            from init_project import _clean_sample_route_sources
            with self.assertRaisesRegex(InitError, "AppRoot sample markers changed"):
                _clean_sample_route_sources(root, SOURCE_APP_PACKAGE)

    def test_clean_profile_journey_uses_bilingual_retained_shell_labels(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            self.write_preflight_fixture(root)
            self.assertEqual(_clean_critical_journey(root, "com.acme.shop", dry_run=False, journey_source_package=SOURCE_APP_PACKAGE), 1)
            journey = root / "baselineprofile/src/main/java/com/thanhng224/androidcomposebase/baselineprofile/CriticalJourney.kt"
            source = journey.read_text(encoding="utf-8")
            for label in ("Get Started", "Bắt đầu", "Home", "Trang chủ", "Settings", "Cài đặt", "Appearance", "Giao diện"):
                self.assertIn(label, source)

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
