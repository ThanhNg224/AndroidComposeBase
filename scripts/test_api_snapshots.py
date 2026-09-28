#!/usr/bin/env python3
"""Focused regression tests for unresolved types in Metalava API signatures."""

from __future__ import annotations

import re
import unittest
from pathlib import Path


BUILD_LOGIC_SOURCE = (
    Path(__file__).parents[1]
    / "build-logic/src/main/kotlin/androidcomposebase/buildlogic/AndroidApiTasks.kt"
)


def unresolved_api_lines(signature_lines: list[str]) -> list[str]:
    source = BUILD_LOGIC_SOURCE.read_text(encoding="utf-8")
    match = re.search(
        r'private val UNRESOLVED_TYPE_PATTERN = Regex\("((?:\\\\.|[^"\\\\])*)"\)',
        source,
    )
    if match is None:
        raise AssertionError("Could not find the production unresolved-type regex in AndroidApiTasks.kt")

    # Kotlin and Python both use Java-style regular expressions; remove Kotlin's string escaping
    # so the cases below exercise the exact pattern used by the Gradle task.
    production_pattern = re.compile(match.group(1).replace("\\\\", "\\"))
    return [line for line in signature_lines if production_pattern.search(line)]


class ApiSnapshotGuardTests(unittest.TestCase):
    def test_metalava_unresolved_placeholders_in_api_signatures_are_rejected(self) -> None:
        signatures = [
            "property public ErrorType buttonHeight;",
            "ctor public AppNavItem(ErrorType selectedIcon);",
            "class public ErrorType extends Any;",
            "method public error.NonExistentClass getLegacyType();",
        ]

        self.assertEqual(unresolved_api_lines(signatures), signatures)

    def test_fully_qualified_api_types_are_accepted(self) -> None:
        signatures = [
            "property public androidx.compose.ui.unit.Dp buttonHeight;",
            "ctor public AppNavItem(androidx.compose.ui.graphics.vector.ImageVector selectedIcon);",
            "class public AppShapes extends androidx.compose.material3.Shapes;",
            "class public AppTypography extends androidx.compose.material3.Typography;",
            "method public error.NonExistentClassLike getValidSimilarType();",
        ]

        self.assertEqual(unresolved_api_lines(signatures), [])


if __name__ == "__main__":
    unittest.main()
