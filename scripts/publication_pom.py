#!/usr/bin/env python3
"""Validate published core POM dependency entries."""

from __future__ import annotations

import argparse
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


FORBIDDEN_COORDINATES = re.compile(
    r"(?:compose|hilt|junit|kotlinx-coroutines-test)", re.IGNORECASE
)


def _child_text(element: ET.Element, name: str) -> str:
    child = next(
        (candidate for candidate in element if candidate.tag.rsplit("}", 1)[-1] == name),
        None,
    )
    return (child.text or "").strip() if child is not None else ""


def forbidden_dependencies(pom_path: Path) -> list[str]:
    """Return dependency coordinates that must not leak from the core artifact."""
    root = ET.parse(pom_path).getroot()
    dependencies = next(
        (child for child in root if child.tag.rsplit("}", 1)[-1] == "dependencies"),
        None,
    )
    if dependencies is None:
        return []

    forbidden: list[str] = []
    for dependency in dependencies:
        if dependency.tag.rsplit("}", 1)[-1] != "dependency":
            continue
        group = _child_text(dependency, "groupId")
        artifact = _child_text(dependency, "artifactId")
        scope = _child_text(dependency, "scope").lower()
        coordinate = f"{group}:{artifact}"
        if scope == "test" or FORBIDDEN_COORDINATES.search(coordinate):
            version = _child_text(dependency, "version")
            forbidden.append(f"{coordinate}:{version} (scope={scope or 'compile'})")
    return forbidden


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pom", type=Path)
    args = parser.parse_args()

    try:
        forbidden = forbidden_dependencies(args.pom)
    except (ET.ParseError, OSError) as error:
        print(f"FAIL: cannot parse POM {args.pom}: {error}", file=sys.stderr)
        return 2

    if forbidden:
        print("FAIL: core POM contains forbidden dependencies:", file=sys.stderr)
        for dependency in forbidden:
            print(f"  - {dependency}", file=sys.stderr)
        return 1

    print(f"PASS: {args.pom} has no Compose, Hilt, or test-only dependencies")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
