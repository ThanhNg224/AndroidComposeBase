# Android 13 UI and Adaptive Layout Smoke

Functional checks on one Samsung handset after the Compose UI, locale handling, and adaptive width updates. This is a smoke report, not a device qualification or a performance benchmark.

## Theme and locale verification (2026-09-25)

The following additional checks were run on the same Samsung `SM-N770F`, Android 13 (API 33), after the theme and locale integration fixes. The instrumentation suite reported **8 tests, 0 failures** at `1032d1e`; the subsequent System-locale reset fix at `454eb9d` also passed its connected instrumentation run. These are device-specific functional checks; API 32 and earlier and foldable hardware were not tested.

| Check | Observed result |
|---|---|
| System theme with device Night mode on | Dark background RGB `(13, 20, 25)` |
| Explicit Light | Light background RGB `(246, 250, 255)` |
| Explicit Dark | Dark background RGB `(13, 20, 25)` |
| Return to System, then device Night mode off | Light background RGB `(246, 250, 255)` |
| Choose Vietnamese in app | Framework per-app locale `[vi-VN]`; Settings selection showed Vietnamese |
| Choose System in app | Framework locale list `[]`; Settings selection showed System |
| Change app locale to `vi-VN` externally while app was backgrounded, then relaunch | Settings selected Vietnamese |
| Reset external app locale | Framework locale list `[]`; Settings selected System |

After testing, the app was uninstalled and the device was restored to Night mode off, font scale `1.1`, resolution `1080×2400`, and density `420`. The process validates the observed theme/locale behavior on this API 33 device; it does not establish startup performance. Android's current [dark theme guidance](https://developer.android.com/develop/ui/views/theming/darktheme) recommends the app-specific `UiModeManager` API on API 31+ and AppCompat on older releases. [Per-app language guidance](https://developer.android.com/guide/topics/resources/app-languages) describes framework locale synchronization from API 33 and the AppCompat `autoStoreLocales` compatibility path (including its blocking disk I/O caveat). The app-specific `MODE_NIGHT_AUTO` mapping for System follows [AOSP `UiModeManagerService`](https://android.googlesource.com/platform/frameworks/base/%2B/refs/heads/android15-release/services/core/java/com/android/server/UiModeManagerService.java).

## Device and artifact provenance

| Checks | Source revision | Artifact |
|---|---|---|
| Onboarding, settings, locale persistence, theme, system bars, counter, weather, startup observations | `6f654e9` | Debug APK |
| Adaptive width and large-window measurements | `d37cb7c` | Debug APK after the content-width fix |
| Baseline profile journey and benchmark test cases | `6f654e9` | Release instrumentation/benchmark variants |

| Device | Value |
|---|---|
| Model | Samsung `SM-N770F` (`RF8NA2EZKZK`) |
| Android | 13, API 33 |
| App package | `com.thanhng224.androidcomposebase` |
| App installation | Not installed before the smoke; uninstalled after the checks |

## UI and behavior checks

- Fresh onboarding in English showed no app navigation. Continuing opened Home.
- In Settings, changing language from English to `vi-VN` translated the UI. After relaunch, Home remained in Vietnamese and the app locale was `[vi-VN]`; the Settings radio selection was not rechecked after relaunch. English was restored afterward.
- Light and Dark app themes were each checked against the opposite system theme. The selected app theme remained independent of system Night mode. Sampled background colors were RGB `(246, 250, 255)` in Light and `(13, 20, 25)` in Dark. System bar icon colors had visible contrast in both mismatched combinations.
- The counter changed from `0` to `1` and retained its value after switching tabs.
- Weather showed cached `26.7°C`. With Wi-Fi and mobile data disabled, refresh displayed **No internet connection** and retained the cached temperature.
- At font scale `2.0`, Settings theme chips wrapped into a `FlowRow`; onboarding kept its primary action visible in portrait and reachable by scrolling in a short `1600×800` window. The UI Kit dialog kept its content and buttons visible.

## Adaptive layout check

On a `2160×1600` window at density `180`, navigation used a rail. Before the width fix (`6f654e9`), the Settings language row spanned `1962 px`; with the debug APK from `d37cb7c`, it measured `720 px`.

This verifies the reported layout on the tested window configuration. No physical foldable was tested.

## Baseline profile and startup observations

- `connectedNonMinifiedReleaseAndroidTest` exited `0` in `187.253 s`; `BaselineProfileGenerator` passed.
- The release benchmark run passed both `StartupBenchmark` cases. Profile generation was skipped in that run. No benchmark timing comparison is reported here.
- Separate debug `am start -W` cold-start observations were approximately `1505 ms`, `1564 ms`, and `1764 ms` across fresh starts. Conditions were not controlled, so these values do not support a performance comparison or guarantee.

## Accessibility and device state

TalkBack was enabled, but its training Activity interrupted the in-app spoken journey; TalkBack behavior is **unverified**. Touch and visual checks do not substitute for a completed TalkBack walkthrough.

Device settings were restored to: resolution `1080×2400`, density `420`, font scale `1.1`, system Night mode `no`, auto-rotate `1`, user rotation `0`, `accessibility_enabled=0`, `enabled_accessibility_services=null`, Wi-Fi `1`, and mobile data `1`. The app was uninstalled after testing.
