# Reusable Core Modules

The reusable library surface consists of two Android library modules. They are DI-agnostic; `:app` owns the Hilt bindings and chooses concrete configuration. Their public signatures are tracked in `core/api/core.api` and `core/ui/api/ui.api`.

## `:core`

The `com.thanhng224.androidcomposebase.core` namespace contains:

- `foundation`: framework-independent `AppDispatchers`, `SettingsStore`, `SettingsKey`, `SecureStore`, and secure-store key contracts.
- `storage`: DataStore-backed settings via `SettingsStoreFactory` and Keystore-backed secure storage via `SecureStoreFactory`.
- `localization`: `AppLanguage`, `SupportedLanguages`, `LocaleManager`, and `AppLocaleApplier` for per-app locale changes.
- `ui.theme`: `AppTheme` and `ThemeManager` for theme persistence/application.
- `network`: `ApiConfig`, `ApiClient`, `ApiResult`, and `NetworkClientFactory`; optional auth and file-transfer contracts are in subpackages.
- `ui.base`: small platform helpers such as `Debouncer` and Flow/lifecycle collection extensions.

The module does not own app configuration, Hilt bindings, feature repositories, Room databases, credentials, or a base URL. Construct or bind its factories from the consuming app's composition root.

## `:core:ui`

`com.thanhng224.androidcomposebase.core.ui` contains the Compose design system and the `AndroidComposeBaseTheme` entry point. Its shared components are `AppPrimaryButton`, `AppSecondaryButton`, `AppOutlinedButton`, `AppTopBar`, and `AppCenterTopBar`; `Dimens`, `AppShapes`, and `AppTypography` provide common design tokens. Use Material 3 directly for dialogs and app navigation. The app owns adaptive navigation through `NavigationSuiteScaffold`.

The `:core:ui` module depends on `:core` for selected foundation/theme support. It does not depend on app features. `UiText` is declared in `:core`; `:core:ui` provides Compose resolution helpers. A consuming app may depend on either module independently according to the APIs it uses.

## `:app` composition

`app/src/main/java/com/thanhng224/androidcomposebase/di/` owns providers and Hilt bindings. Feature contracts and implementations remain under their feature packages. The sample weather database and network setup remain app-owned examples and are not bundled into `:core`.

## API and compatibility

Before changing a public type, inspect the current API dump and every caller. Breaking API changes are allowed while there is no real downstream consumer and no API freeze has been requested. In every case, review the generated `apiDump` diff and run the matching `apiCheck`; update the snapshot only after that review. Keep app-only features and contracts out of published APIs.

## Publication checks

`scripts/verify-publication.sh --quick` publishes both release variants to a temporary local Maven repository and compiles isolated consumers. `--release` also runs the release consumer checks. The POM validator inspects dependency entries rather than searching the whole metadata document.

Version `0.1.0` is configured locally in `core/gradle.properties`. No release tag or remote artifact availability is implied. Before a release, update the version intentionally, run the consumer gate, review the generated POM and API diffs, then follow the separately approved publishing process.
