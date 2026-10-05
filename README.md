# AndroidComposeBase

An Android application starter with Jetpack Compose and reusable core libraries.

## Prerequisites

Use Python 3.12+, Android Studio with JDK 21, and an Android SDK matching the checked-in Gradle configuration. The Gradle wrapper owns the build-tool version.

## Initialize

Clone into your new project directory, then run:

```bash
python3 scripts/init_project.py --project-name AcmeShop --app-name "Acme Shop" --package com.acme.shop --scope full --clean-samples
```

Add `--dry-run` to preview first. Omit `--clean-samples` to retain demos, or use `--scope app-only` to preserve the library namespace. The initializer checks the recognized source layout and runs its build gate unless `--skip-build-check` is explicit. See `--help` and [Git workflow](docs/GIT_FLOW.md) for the existing derived-project Gitflow behavior.

## Prepare

Open the initialized directory in Android Studio with JDK 21 and sync Gradle. Add application features following [the feature guide](docs/FEATURE_TEMPLATE.md); keep reusable modules independent of app features.

## Verify

Use the risk levels and exact commands in [Verification](docs/VERIFICATION.md), including Python tooling tests, four archive smoke modes, and the existing publication gates. A local result is separate from remote CI and device proof.

## Run

Run the `:app` configuration on an Android emulator or device. Generate a fresh baseline profile after defining your application's journeys. See [Core modules](docs/CORE_MODULES.md) for theme/locale behavior and library integration.

## Further reading

- [Agent workflow](AGENTS.md) and [Git workflow](docs/GIT_FLOW.md)
- [Architecture](docs/ARCHITECTURE.md) and [Standards](docs/STANDARD.md)
- [Core modules, API and publication](docs/CORE_MODULES.md)
- [Design system](docs/DESIGN_SYSTEM.md) and [Android 13 smoke](docs/performance/ANDROID_13_SMOKE.md)

The current source version is `0.1.0`; this README does not establish a matching release tag or published artifact. Dependency automation prerequisites and the recorded remote status remain in the Git workflow guide.
