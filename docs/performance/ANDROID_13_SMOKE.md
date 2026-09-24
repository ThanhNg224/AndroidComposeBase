# Android 13 Device Smoke

This is a functional smoke check and a small startup sample for the AndroidComposeBase 0.1.0 working tree. It is not a macrobenchmark, a release-device qualification, or a performance guarantee for other devices.

## Environment

| Item | Value |
|---|---|
| Source revision | `2862bf1` (`main`; the measured APK was built from this tree) |
| Device | Samsung `SM-N770F` (`RF8NA2EZKZK`) |
| Android | 13, API 33 |
| Variant | `debug`, installed from `app/build/outputs/apk/debug/app-debug.apk` |
| App package | `com.thanhng224.androidcomposebase` (not installed before this smoke) |
| Network before and after | `wifi_on=1`, `mobile_data=1` |

## Functional checks

- Fresh install succeeded. The first `am start -W` reported `Status: ok`, `LaunchState: COLD`, `TotalTime: 1470 ms`, and `WaitTime: 1478 ms`.
- Onboarding showed **Get Started**. Tapping it saved completion and opened Home. Subsequent cold launches opened the app without showing onboarding again.
- Settings opened. The Light appearance option became selected; language changed from English to Vietnamese and its radio selection updated. Theme and language were restored to System and English after the check.
- Demo counter changed from `0` to `1` after one increment.
- Weather loaded and remained visible after an online refresh: `28.3°C`, `12.3 km/h`; the refresh showed no error.
- With both Wi-Fi and mobile data disabled, refreshing displayed **No internet connection** while cached `28.3°C` remained visible. A `finally` path restored the original network toggles, and both were read back as `1`.

## Startup smoke

Three additional cold launches used `adb shell am force-stop` followed by `adb shell am start -W -n com.thanhng224.androidcomposebase/.MainActivity`:

| Run | Launch state | TotalTime | WaitTime |
|---:|---|---:|---:|
| 1 | COLD | 1399 ms | 1402 ms |
| 2 | COLD | 1395 ms | 1398 ms |
| 3 | COLD | 1386 ms | 1388 ms |

The observed `TotalTime` range was 1386–1399 ms (median 1395 ms). These three `am start -W` observations do not control thermal state, background load, compilation mode, or repeated device conditions; they are startup smoke values only.

## Baseline profile status

The stale committed `app/src/release/generated/baselineProfiles/baseline-prof.txt` from the earlier XML sample journey was removed. The release APK built after that removal still contains compiled `assets/dexopt/baseline.prof` and `baseline.profm` files (5915 and 887 bytes). The generated merged, combined, and R8 profile text intermediates contained no `DemoFragment` or `DesignSystemFragment` references. The binary assets are expected build outputs; their presence does not mean the old app journey remains.

`:baselineprofile:compileBenchmarkReleaseKotlin` passed. Profile collection with `:app:generateBaselineProfile` and macrobenchmark measurement with `:baselineprofile:connectedCheck` were not run as part of this smoke, so there are no current baseline-profile timing results. Generate a fresh profile after defining the journeys for a consuming app.

## Limits

The device check used one Android 13 handset and one newly installed debug app. It did not test a minified APK on-device, sustained frame rendering, battery or memory use, a range of API levels, or startup under controlled benchmark conditions. Remote CI was not run from this local checkout.
