from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from publication_pom import forbidden_dependencies


class PublicationPomTests(unittest.TestCase):
    def parse(self, contents: str) -> list[str]:
        with tempfile.TemporaryDirectory() as tmpdir:
            pom = Path(tmpdir) / "pom.xml"
            pom.write_text(contents, encoding="utf-8")
            return forbidden_dependencies(pom)

    def test_project_metadata_can_mention_compose(self) -> None:
        forbidden = self.parse(
            """<project>
              <name>AndroidComposeBase: Compose starter</name>
              <description>A base app using Compose</description>
              <dependencies><dependency><groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-stdlib</artifactId><version>2.4.20</version>
              </dependency></dependencies>
            </project>"""
        )
        self.assertEqual(forbidden, [])

    def test_dependency_coordinates_and_test_scope_are_checked(self) -> None:
        forbidden = self.parse(
            """<project xmlns="http://maven.apache.org/POM/4.0.0">
              <dependencies>
                <dependency><groupId>androidx.compose.ui</groupId>
                  <artifactId>ui</artifactId><version>1.0</version>
                </dependency>
                <dependency><groupId>org.junit</groupId>
                  <artifactId>junit</artifactId><version>4.13</version><scope>test</scope>
                </dependency>
                <dependency><groupId>org.example</groupId>
                  <artifactId>kotlinx-coroutines-test</artifactId><version>1.0</version>
                </dependency>
              </dependencies>
            </project>"""
        )
        self.assertEqual(len(forbidden), 3)
        self.assertIn("androidx.compose.ui:ui:1.0", forbidden[0])
        self.assertIn("org.junit:junit:4.13", forbidden[1])
        self.assertIn("org.example:kotlinx-coroutines-test:1.0", forbidden[2])


if __name__ == "__main__":
    unittest.main()
