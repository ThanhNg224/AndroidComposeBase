# Changelog

Changes to AndroidComposeBase are recorded here. This file describes source changes; a release is available only after its release process and artifact verification complete.

## 0.1.0 — unreleased

- First AndroidComposeBase starter release candidate.
- Keeps `:app`, `:core`, `:core:ui`, and `:baselineprofile` as the project modules.
- Includes the onboarding app shell, settings, Room-backed offline weather sample, reusable Compose UI, initializer, local publication checks, and CI workflow.
- Breaking `:core` API changes move reusable theme, text, and common helpers from `core.ui.*` to `core.theme`, `core.text`, and `core.common`; `AppSettingsKeys` now exports only `THEME_MODE`, with onboarding completion owned by the onboarding feature.
- Authenticated requests now use the configurable `Bearer` authorization scheme by default, and failed refreshes emit `AuthSession.sessionExpired`.
- Replaces Navigation Compose with Navigation 3. Features now own serializable route keys and entry registrations; the app shell composes them and preserves a separate back stack per top-level tab.
- Adds reusable loading, empty, and error states in `:core:ui` and removes the extended Material icons dependency.
- Simplifies Settings and Demo ViewModels by removing pass-through use cases while retaining the Demo counter increment use case for its named behavior.
- Removes the sample app's fake login flow and the corresponding `AppSettingsKeys.IS_LOGGED_IN` core API.
- No release tag or published JitPack artifact is associated with this version yet.

Previous AndroidCoreBase history is preserved in [the archive](docs/archive/androidcorebase/README.md).
