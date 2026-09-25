# Reusable Core Modules

The reusable library surface consists of two Android library modules. They are DI-agnostic; `:app` owns the Hilt bindings and chooses concrete configuration. Their public signatures are tracked in `core/api/core.api` and `core/ui/api/ui.api`.

## `:core`

The `com.thanhng224.androidcomposebase.core` namespace contains:

- `foundation`: framework-independent `AppDispatchers`, `SettingsStore`, `SettingsKey`, `SecureStore`, and secure-store key contracts.
- `storage`: DataStore-backed settings via `SettingsStoreFactory` and Keystore-backed secure storage via `SecureStoreFactory`.
- `localization`: `AppLanguage`, `SupportedLanguages`, `LocaleManager`, and `AppLocaleApplier` for per-app locale changes.
- `theme`: `AppTheme` and `ThemeManager` for theme persistence/application.
- `text`: `UiText`, a localizable UI message type resolved via `UiText.resolve(context)`.
- `network`: `ApiConfig`, `ApiClient`, `ApiResult`, and `NetworkClientFactory`; optional auth and file-transfer contracts are in subpackages.

### Network auth and error handling

`AuthTokenInterceptor` and `TokenAuthenticator` format the `Authorization` header as `"$scheme $token"` (replacing any existing header rather than duplicating it); the scheme defaults to `DEFAULT_AUTH_SCHEME` ("Bearer") and a blank scheme sends the raw token. `TokenAuthenticator` compares the failed request's `Authorization` header against the cached token formatted the same way before deciding whether to retry with the cached token or invoke the configured refresher. When a refresher is configured and returns null (refresh failed, or there was no refresh token), the authenticator clears `AuthSession` and emits on `AuthSession.sessionExpired` (a `Flow<Unit>`) so the app can react (e.g. sign the user out); with no refresher configured, it returns null without clearing anything. `RetrofitApiClient` surfaces non-2xx responses as `ApiFailure.Http` with the response's error body text (truncated to 2,048 characters) when present and non-blank, falling back to the HTTP status message otherwise. `NetworkClientFactory.defaultJson` (`ignoreUnknownKeys = true`, `explicitNulls = false`) is the default used by `createRetrofit` and can be overridden per call. The Kotlin serialization Retrofit converter is the official `com.squareup.retrofit2:converter-kotlinx-serialization` artifact.
- `common`: small platform helpers such as `Debouncer` and Flow/lifecycle collection extensions.

The module does not own app configuration, Hilt bindings, feature repositories, Room databases, credentials, or a base URL. Construct or bind its factories from the consuming app's composition root.

`AppSettingsKeys` contains only the reusable theme preference key (`THEME_MODE`). Onboarding completion is app-feature state, so the onboarding repository owns that key instead of exporting it from `:core`.

### Theme and locale behavior

`:core` exposes `ThemeManager` and `LocaleManager`; the consuming app owns when and where to call them. Theme preference is stored in the app's DataStore. The app applies it through the platform app-specific night-mode API on Android 12 (API 31) and later, and through `AppCompatDelegate` on API 30 and earlier. `SYSTEM` clears the app-specific override/follows the device setting. On API 31+, the app maps `SYSTEM` to `UiModeManager.MODE_NIGHT_AUTO`; AOSP maps that app-specific value to an undefined package night qualifier, which lets the system configuration decide. This is distinct from setting the device-wide night mode. See [Android dark theme guidance](https://developer.android.com/develop/ui/views/theming/darktheme) and [AOSP `UiModeManagerService`](https://android.googlesource.com/platform/frameworks/base/%2B/refs/heads/android15-release/services/core/java/com/android/server/UiModeManagerService.java).

AppCompat/platform application locales are the sole persisted locale source. Android 13 (API 33) and later reads and writes the framework per-app locale, which is shared with Android Settings. On API 32 and earlier, AppCompat's `autoStoreLocales=true` stores the selection for backward compatibility; Android's guidance documents that this may perform a blocking main-thread disk read/write. The Settings feature does not maintain a second locale preference. Choosing **System** clears the app locale override, so the framework reports an empty locale list and the device locale is used. The hosting Activity must extend `AppCompatActivity` for the backward-compatible AppCompat locale API. See [per-app language guidance](https://developer.android.com/guide/topics/resources/app-languages).

Startup waits up to two seconds for the persisted theme read before releasing the splash screen. A read that finishes later still applies the selected theme; the timeout bounds splash waiting and is not a performance measurement or guarantee. Dynamic Material 3 color remains available in `:core:ui` where supported and enabled.

## `:core:ui`

`com.thanhng224.androidcomposebase.core.ui` contains the Compose design system and the `AndroidComposeBaseTheme` entry point. Its shared components include `AppPrimaryButton`, `AppSecondaryButton`, `AppOutlinedButton`, `AppTopBar`, `AppCenterTopBar`, `AppFloatingNavBar`, and the `AppLoadingState`, `AppEmptyState`, and `AppErrorState` screen states; `Dimens`, `AppShapes`, and `AppTypography` provide common design tokens. Use Material 3 directly for dialogs. `:app` owns Navigation 3 assembly and uses `AppFloatingNavBar` for its top-level tabs.

The `:core:ui` module depends on `:core` for selected foundation/theme support. It does not depend on app features. `UiText` is declared in `:core`; `:core:ui` provides Compose resolution helpers. A consuming app may depend on either module independently according to the APIs it uses.

## `:app` composition

`app/src/main/java/com/thanhng224/androidcomposebase/di/` owns providers and Hilt bindings. Feature contracts and implementations remain under their feature packages. The sample weather database and network setup remain app-owned examples and are not bundled into `:core`.

## API and compatibility

Before changing a public type, inspect the current API dump and every caller. Breaking API changes are allowed while there is no real downstream consumer and no API freeze has been requested. In every case, review the generated `apiDump` diff and run the matching `apiCheck`; update the snapshot only after that review. Keep app-only features and contracts out of published APIs.

## Publication checks

`scripts/verify-publication.sh --quick` publishes both release variants to a temporary local Maven repository and compiles isolated consumers. `--release` also runs the release consumer checks. The POM validator inspects dependency entries rather than searching the whole metadata document.

Version `0.1.0` is configured locally in `core/gradle.properties`. No release tag or remote artifact availability is implied. Before a release, update the version intentionally, run the consumer gate, review the generated POM and API diffs, then follow the separately approved publishing process.
