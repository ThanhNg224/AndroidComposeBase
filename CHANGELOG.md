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
- Removes the unused `AppSettingsKeys.FIRST_OPEN_AT`, `OPEN_COUNT`, `DEBUG_LOGGING_ENABLED`, and `ONBOARDING_COMPLETED` core API entries.
- Removes the unused View-era `Bundle`/`Intent` argument-delegate helpers (`ArgumentDelegates.kt`, `BundleCompat.kt`) from `:core`, dead now that navigation is Navigation 3-based.
- `TokenAuthenticator` no longer treats a 401 with no `Authorization` header and no cached token (e.g. a failed login request) as an expired session: it returns null without invoking the refresher, clearing `AuthSession`, or emitting `sessionExpired`.
- Adds `Dimens.maxContentWidth`, `maxReadableWidth`, and `maxNavBarWidth` width-cap tokens to `:core:ui` and replaces the screens' hard-coded width literals with them; removes `Dimens.floatingNavBarClearance` in favor of measuring the floating nav bar's actual height.
- No release tag or published JitPack artifact is associated with this version yet.

Previous AndroidCoreBase history is preserved in [the archive](docs/archive/androidcorebase/README.md).
