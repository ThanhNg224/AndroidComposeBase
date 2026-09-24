# AndroidComposeBase

AndroidComposeBase is a cloneable Android application base using Kotlin, Jetpack Compose, Material 3, Navigation Compose, Hilt, Room, DataStore, and coroutines. It keeps the current Gradle modules small and places application features in `:app` until a real reuse boundary justifies extraction.

## Start a project

Clone this repository, then initialize it from the repository root. The default `full` scope renames the app and core packages; `app-only` renames the app while retaining the `:core` package. `--clean-samples` removes the weather and design-system demos and keeps the app shell, theme, navigation, onboarding, and settings.

```bash
python3 scripts/init_project.py \
  --project-name AcmeShop \
  --app-name "Acme Shop" \
  --package com.acme.shop \
  --scope full \
  --clean-samples
```

Omit `--clean-samples` to keep the offline weather and design-system examples. Choose `--scope app-only` if the reusable core namespace should remain `com.thanhng224.androidcomposebase.core`. The initializer validates recognized source markers and builds the result unless `--skip-build-check` is supplied. See `python3 scripts/init_project.py --help` for every option.

Open the initialized directory in Android Studio with JDK 21, sync Gradle, then run the `:app` configuration on an emulator or device. The app shell adapts navigation between a bottom bar and navigation rail; feature screens should use Compose insets and scrollable content instead of reserving fixed space for navigation. Add product capabilities under `app/src/main/java/<package>/feature/`; see [the feature guide](docs/FEATURE_TEMPLATE.md) and [architecture](docs/ARCHITECTURE.md).

## Modules

| Module | Responsibility |
|---|---|
| `:app` | Application composition root, Hilt graph, navigation, onboarding, settings, and sample features. |
| `:core` | Reusable Android foundation: storage contracts and factories, localization, network, secure storage, dispatchers, and theme state. |
| `:core:ui` | Compose theme and reusable Compose components. |
| `:baselineprofile` | Macrobenchmark target and baseline-profile journey for the sample app. |

The dependency direction inside a feature is presentation to domain contracts, with data implementing those contracts. `:app` assembles features and shared infrastructure. See [core modules](docs/CORE_MODULES.md) for the reusable library boundaries.

## Local checks

```bash
./gradlew check
./gradlew :app:assembleRelease
./gradlew :core:apiCheck :core:ui:apiCheck
python3 -m unittest discover -s scripts -p 'test_*.py' --verbose
python3 scripts/smoke_init_project.py --build full-clean
./scripts/verify-publication.sh --release
```

The repository workflow runs Python checks, initializer archive smoke, Gradle checks, `:core:assembleRelease`, and an isolated publication consumer. The local app release gate is `:app:assembleRelease`. A green local run does not establish that GitHub Actions or branch protection is configured on the remote.

Dependabot auto-merge is inactive until remote setup is complete. Protect `main` with classic branch protection and require the `check` status, enable repository auto-merge, and add a fine-grained Actions secret named `BRANCH_PROTECTION_READ_TOKEN` with repository Administration:read. The metadata workflow uses the read-only `GITHUB_TOKEN` and publishes only a bounded eligibility result, PR number, and PR head SHA. A separate `workflow_run` workflow validates that artifact and the current PR, then uses its narrowly permissioned `GITHUB_TOKEN` to enable auto-merge. It accepts patch and minor updates only, does not auto-approve, and fails closed when the required branch protection, repository setting, or secret is missing. Ensure the repository or organization Actions policy permits the workflow's `contents:write` and `pull-requests:write` token permissions. For public repositories affected by GitHub's default event policy, explicitly allow `pull_request_target` for this workflow ([event policy guidance](https://docs.github.com/en/actions/reference/security/securely-using-pull_request_target)). This repository has not enabled or verified those remote settings.

## Library publication

`:core` and `:core:ui` are configured for Maven publication and have isolated local consumer checks. Version `0.1.0` is the current source version; no matching release tag or published JitPack artifact is claimed here. Before changing a public API, inspect the API dump and its callers. Breaking changes are allowed while no real downstream consumer exists and no API freeze has been requested; still review the generated `apiDump` diff and run the matching `apiCheck`. See the [publication guide](docs/CORE_MODULES.md#api-and-compatibility) before consuming these modules outside this repository.

## Further reading

- [Architecture](docs/ARCHITECTURE.md)
- [Kotlin and Android standards](docs/STANDARD.md)
- [Git workflow](docs/GIT_FLOW.md)
- [Feature template](docs/FEATURE_TEMPLATE.md)
- [Compose design system](docs/DESIGN_SYSTEM.md)
- [Current Android 13 device smoke](docs/performance/ANDROID_13_SMOKE.md)
