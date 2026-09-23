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
    _move,
    escape_android_string_resource,
    valid_package,
    valid_project_name,
    validate_source,
)


class InitializerUnitTests(unittest.TestCase):
    def test_validates_names_and_packages(self) -> None:
        self.assertTrue(valid_project_name("AcmeShop_2"))
        self.assertFalse(valid_project_name("acmeShop"))
        self.assertTrue(valid_package("com.acme.shop"))
        self.assertFalse(valid_package("com.when.shop"))
        self.assertFalse(valid_package("com.acme..shop"))

    def test_android_string_escaping_handles_resource_prefix_and_quotes(self) -> None:
        self.assertEqual(escape_android_string_resource("@New's \"Shop\""), "\\@New\\'s \\\"Shop\\\"")

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
