#!/usr/bin/env python3
"""Exercise initializer modes on clean copies produced by `git archive`.

All four combinations are checked for package, route, resource, dependency, and
profile residue. A representative clean clone can also be built with `--build full-clean`
or `--build app-only-clean`.
"""

from __future__ import annotations

import argparse
import hashlib
import subprocess
import sys
import tarfile
import tempfile
import xml.etree.ElementTree as ET
from io import BytesIO
from pathlib import Path

APP_SOURCE = "com.thanhng224.androidcomposebase"
CORE_SOURCE = f"{APP_SOURCE}.core"


def archive_revision(repo: Path, revision: str, destination: Path) -> None:
    archive = subprocess.run(["git", "archive", "--format=tar", revision], cwd=repo, check=True, capture_output=True).stdout
    with tarfile.open(fileobj=BytesIO(archive), mode="r:") as bundle:
        bundle.extractall(destination, filter="data")


def text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def tree_digest(root: Path) -> str:
    digest = hashlib.sha256()
    for path in sorted(item for item in root.rglob("*") if item.is_file()):
        digest.update(path.relative_to(root).as_posix().encode("utf-8"))
        digest.update(path.read_bytes())
    return digest.hexdigest()


def check_clone(clone: Path, scope: str, clean: bool, build: bool) -> None:
    suffix = "Clean" if clean else "Samples"
    name = f"Smoke{scope.title().replace('-', '')}{suffix}"
    app_package = f"org.example.{name.lower()}"
    command = [
        sys.executable,
        str(clone / "scripts/init_project.py"),
        "--project-name", name,
        "--app-name", "Smoke & Demo \"App\"",
        "--package", app_package,
        "--scope", scope,
        "--force",
        "--skip-build-check",
    ]
    if clean:
        command.append("--clean-samples")
    before_dry_run = tree_digest(clone)
    dry_run = subprocess.run([*command, "--dry-run"], cwd=clone, check=True, capture_output=True, text=True)
    assert "Dry run complete" in dry_run.stdout
    assert tree_digest(clone) == before_dry_run, "dry-run modified the archive clone"
    subprocess.run(command, cwd=clone, check=True)

    app_build = text(clone / "app/build.gradle.kts")
    assert f'namespace = "{app_package}"' in app_build
    assert f'applicationId = "{app_package}"' in app_build
    settings = text(clone / "settings.gradle.kts")
    assert f'rootProject.name = "{name}"' in settings
    app_dir = clone / "app/src/main/java" / Path(*app_package.split("."))
    assert (app_dir / f"{name}Application.kt").is_file()
    app_root = text(app_dir / "presentation/AppRoot.kt")
    routes = text(app_dir / "navigation/ScreenRoute.kt")
    onboarding = text(app_dir / "feature/onboarding/presentation/ui/OnboardingScreen.kt")
    home = text(app_dir / "appshell/home/HomeScreen.kt")
    for relative in ("app/src/main/res/values/strings.xml", "app/src/main/res/values-vi/strings.xml"):
        strings = ET.parse(clone / relative).getroot()
        app_name = strings.find("string[@name='app_name']")
        assert app_name is not None and app_name.text.replace('\\"', '"') == 'Smoke & Demo "App"'
    assert '"AndroidComposeBase"' not in onboarding
    assert '"AndroidComposeBase"' not in home
    assert not (clone / "app/src/release/generated/baselineProfiles/baseline-prof.txt").exists()
    sample_dirs = (app_dir / "sample/demo", app_dir / "sample/designsystem")
    if clean:
        assert all(not path.exists() for path in sample_dirs)
        assert not (app_dir / "di/MetadataLoggingInterceptor.kt").exists()
        test_samples = clone / "app/src/test/java" / Path(*app_package.split(".")) / "sample"
        assert not (test_samples / "demo").exists() and not (test_samples / "designsystem").exists()
        assert "ScreenRoute.Demo" not in app_root and "ScreenRoute.DesignSystem" not in app_root
        assert "Demo" not in routes and "DesignSystem" not in routes
        assert "sample.demo" not in app_root and "sample.designsystem" not in app_root
        assert "Retrofit" not in app_build and "Room" not in app_build
        assert "libs.retrofit" not in app_build and "libs.room" not in app_build and "libs.kotlinx.serialization.json" not in app_build
        assert "API_BASE_URL" not in app_build
        assert 'android.permission.INTERNET' not in text(clone / "app/src/main/AndroidManifest.xml")
        for relative in ("app/src/main/res/values/strings.xml", "app/src/main/res/values-vi/strings.xml"):
            strings = ET.parse(clone / relative).getroot()
            sample_strings = [node.attrib["name"] for node in strings if node.attrib.get("name", "").startswith(("demo_", "design_system_")) or node.attrib.get("name") in {"navigation_demo", "navigation_design"}]
            assert not sample_strings, f"sample strings remain: {sample_strings}"
        journey = text(clone / "baselineprofile/src/main/java" / Path(*app_package.split(".")) / "baselineprofile/CriticalJourney.kt")
        assert "Demo" not in journey and "Design" not in journey and "weather" not in journey.lower()
    else:
        assert all(path.is_dir() for path in sample_dirs)
        assert (app_dir / "di/MetadataLoggingInterceptor.kt").is_file()
        assert "ScreenRoute.Demo" in app_root and "ScreenRoute.DesignSystem" in app_root

    if scope == "full":
        core_package = f"{app_package}.core"
        assert f'namespace = "{core_package}"' in text(clone / "core/build.gradle.kts")
        assert f'namespace = "{core_package}.ui"' in text(clone / "core/ui/build.gradle.kts")
        assert (clone / "core/src/main/java" / Path(*core_package.split("."))).is_dir()
    else:
        assert f'namespace = "{CORE_SOURCE}"' in text(clone / "core/build.gradle.kts")
        assert f'namespace = "{CORE_SOURCE}.ui"' in text(clone / "core/ui/build.gradle.kts")
        assert "AndroidComposeBaseTheme" in app_root

    if build:
        subprocess.run(
            [str(clone / "gradlew"), "-p", str(clone), ":app:assembleDebug", "--no-daemon", "--console=plain"],
            cwd=clone,
            check=True,
        )
        assert (clone / "app/build/outputs/apk/debug/app-debug.apk").is_file()
    print(f"PASS archive clone: scope={scope}, clean_samples={clean}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path(__file__).resolve().parent.parent)
    parser.add_argument("--revision", default="HEAD")
    parser.add_argument("--keep-dir", type=Path, help="Keep the four initialized archive clones at this new directory")
    parser.add_argument("--build", choices=("full-clean", "app-only-clean"), help="Also build one representative clean clone")
    args = parser.parse_args()
    repo = args.repo.resolve()

    def run_smokes(base: Path) -> None:
        for scope in ("full", "app-only"):
            for clean in (False, True):
                clone = base / f"{scope}-{'clean' if clean else 'samples'}"
                clone.mkdir()
                archive_revision(repo, args.revision, clone)
                check_clone(clone, scope, clean, args.build == f"{scope}-{'clean' if clean else 'samples'}")

    if args.keep_dir:
        base = args.keep_dir.resolve()
        if base.exists():
            parser.error(f"--keep-dir must not already exist: {base}")
        base.mkdir(parents=True)
        run_smokes(base)
        print(f"Archive clones retained at {base}")
    else:
        with tempfile.TemporaryDirectory(prefix="androidcomposebase-init-smoke-") as temp:
            run_smokes(Path(temp))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
