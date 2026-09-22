# AndroidComposeBase

[![JitPack](https://jitpack.io/v/ThanhNg224/AndroidComposeBase.svg)](https://jitpack.io/#ThanhNg224/AndroidComposeBase)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-blue.svg)](https://kotlinlang.org)
[![MinSDK](https://img.shields.io/badge/MinSDK-24-green.svg)](https://developer.android.com)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-37-brightgreen.svg)](https://developer.android.com)
[![JDK](https://img.shields.io/badge/JDK-21-orange.svg)](https://www.oracle.com/java)

A modern, production-grade Android starter and architecture foundation built **100% with Jetpack Compose**, **Material 3 Design System**, **Offline-First Room Caching**, **Type-Safe Navigation**, **Coroutines & StateFlow**, and **Clean Architecture**.

`:core` (headless foundation) and `:core:ui` (Compose design system) are reusable, dependency-injection-agnostic libraries; `:app` is both the reference application and a ready-to-use production starter.

---

## 🌟 Highlights

- **100% Pure Jetpack Compose**: Zero XML layouts. Declarative UI everywhere with Material 3 Expressive theming.
- **Type-Safe Navigation**: Built with Jetpack Navigation Compose 2.8+ using Kotlinx Serialization (`@Serializable` route contracts).
- **Offline-First Architecture**: Room 2.7 caching with single source of truth, reactive `Flow` observation, and network synchronization.
- **Compose Design System (`:core:ui`)**: Reusable UI components including `FloatingNavBar`, `AppDialog`, `AsyncContent`, `AppButton`, `AppTopBar`, unified dimensions (`Dimens`), and tokens.
- **Clean Architecture & MVI/MVVM**: Strict unidirectional data flow, immutable `UiState`, transient events modeled via acknowledged FIFO `pendingMessages` (survives process death & configuration change).
- **Dynamic Theming & Per-App Language**: System/Light/Dark theme management backed by Jetpack DataStore, plus Android 13+ per-app locale selection.
- **Robust Security**: Android Keystore-backed `SecureStore` (via `EncryptedSharedPreferences`), single-flight token refresh `Authenticator`, and database passphrase management.
- **Strict Quality Gates**: Detekt, KtLint, Android Lint (`abortOnError`), Metalava public API tracking, Kover test coverage, and Baseline Profiles.

---

## 🚀 Quick Start: Initializing a New Project

When cloning this repository to build a new app, run the automated setup script to rename and refactor the base in seconds:

### Quick Setup Commands:

```bash
# 1. Clone into your new project folder
git clone https://github.com/ThanhNg224/AndroidComposeBase.git my-awesome-app
cd my-awesome-app

# 2. Run the interactive wizard (prompts for project name, package, sample code pruning, etc.)
python3 scripts/init_project.py
```

Or run directly in non-interactive CLI mode:

```bash
python3 scripts/init_project.py \
  --project-name "AcmeShop" \
  --app-name "Acme Shop" \
  --package "com.acme.shop" \
  --clean-samples
```

### Next Steps for Developers:
1. **Open in Android Studio**: Open the cloned directory in Android Studio (Ladybug / Koala or newer, JDK 21 configured).
2. **Gradle Sync & Run**: Let Gradle sync and run `:app` on your Android device or emulator.
3. **Build Features**: Add your production features under `app/src/main/java/<package>/feature/<name>/` following our Clean Architecture guide in [docs/FEATURE_TEMPLATE.md](docs/FEATURE_TEMPLATE.md) and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
4. **Baseline Profiles**: After creating your key user journeys, generate a tailored baseline profile with:
   ```bash
   ./gradlew :baselineprofile:generateBaselineProfile
   ```

---

## 🏗️ Project Architecture & Modular Structure

```
AndroidComposeBase/
├── core/                                                # Reusable headless library (published to JitPack)
│   └── src/main/java/com/thanhng224/androidcomposebase/core/
│       ├── architecture/                                # DefaultAppDispatchers
│       ├── foundation/                                  # Pure contracts: AppDispatchers, SettingsStore, SecureStore
│       ├── localization/                                # AppLanguage, LocaleManager, AppCompatLocaleApplier
│       ├── network/                                     # ApiClient/ApiResult, NetworkClientFactory, auth/, transfer/
│       └── storage/                                     # settings/ (DataStore), secure/ (Keystore-backed SecureStore)
│
├── core/ui/                                             # Compose Design System (published to JitPack)
│   └── src/main/java/com/thanhng224/androidcomposebase/core/ui/
│       ├── base/                                        # BaseComposeActivity, ComposeInterop
│       ├── component/                                   # FloatingNavBar, AppDialog, AsyncContent, AppButton, AppTopBar
│       ├── extensions/                                  # UiText, stringResource extensions
│       └── theme/                                       # Color, Theme, Typography, Shape, Dimens
│
├── app/                                                 # Application shell, Hilt DI graph, and feature implementations
│   └── src/main/java/com/thanhng224/androidcomposebase/
│       ├── AndroidComposeBaseApplication.kt             # Application entry point, Timber logging, bounded startup
│       ├── MainActivity.kt                              # Single-Activity shell hosting AppRoot
│       ├── appshell/                                    # AppRoot, AppViewModel, Bottom Nav Shell
│       ├── navigation/                                  # ScreenRoute, AppNavHost (type-safe Navigation Compose)
│       ├── database/                                    # Room AppDatabase, DAOs, Entities, Mappers
│       ├── di/                                          # Hilt modules (AppCoreModule, NetworkModule, DatabaseModule)
│       ├── feature/                                     # Concrete features (Home, Settings, Demo/Weather)
│       ├── designsystem/                                # Design system catalog & showcase
│       └── startup/                                     # AppStartupCoordinator
│
└── baselineprofile/                                     # Macrobenchmark module: Baseline Profile generation & startup benchmarks
```

---

## 🛠️ Tech Stack & Specifications

| Component | Specification / Technology |
|---|---|
| **Language & JDK** | Kotlin 2.4 / Java 21 |
| **SDK Compatibility** | Min SDK 24 (Android 7.0) / Target SDK 37 (Android 15) |
| **UI Toolkit** | 100% Jetpack Compose, Material 3, Coil 3 (image loading) |
| **Architecture** | Clean Architecture, MVVM / MVI, Single-Activity pattern |
| **Navigation** | Jetpack Navigation Compose 2.8+ (Type-safe with Kotlinx Serialization) |
| **Dependency Injection** | Hilt (Dagger) + KSP in `:app`. `:core` and `:core:ui` are DI-agnostic. |
| **Local Storage** | Room 2.7 (SQLite / Offline-First), Jetpack DataStore Preferences, Android Keystore |
| **Network & Serialization** | Retrofit 3, OkHttp 5, Kotlinx Serialization |
| **Async & Concurrency** | Kotlin Coroutines, StateFlow, WorkManager |
| **Code Quality & Gates** | Detekt, KtLint, Android Lint (`abortOnError`), Metalava API tracking, Kover |
| **Performance** | AndroidX Baseline Profiles & Macrobenchmark |

---

## 📦 Consuming `:core` & `:core:ui` via JitPack

### 1. Add the JitPack repository

In your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### 2. Add dependencies

In your module's `build.gradle.kts`:

```kotlin
dependencies {
    // Headless core (networking, dispatchers, storage, secure store, localization)
    implementation("com.github.ThanhNg224.AndroidComposeBase:AndroidComposeBase:v2.0.1")

    // Jetpack Compose Design System (Material 3 tokens, theme, components)
    implementation("com.github.ThanhNg224.AndroidComposeBase:AndroidComposeBase-ui:v2.0.1")

    // Test fixtures for unit testing
    testImplementation(testFixtures("com.github.ThanhNg224.AndroidComposeBase:AndroidComposeBase:v2.0.1"))
}
```

---

## 💡 Key Architectural Patterns

### 1. Type-Safe Navigation

Routes are declared as type-safe `@Serializable` objects or classes:

```kotlin
@Serializable
sealed interface ScreenRoute {
    @Serializable data object Onboarding : ScreenRoute
    @Serializable data object Login : ScreenRoute
    @Serializable data object Home : ScreenRoute
    @Serializable data object Settings : ScreenRoute
    @Serializable data object Demo : ScreenRoute
    @Serializable data class Detail(val id: String) : ScreenRoute
}
```

Navigating in `AppNavHost`:

```kotlin
NavHost(
    navController = navController,
    startDestination = ScreenRoute.Home,
) {
    composable<ScreenRoute.Home> {
        HomeScreen(
            onNavigateToSettings = { navController.navigate(ScreenRoute.Settings) },
            onNavigateToDemo = { navController.navigate(ScreenRoute.Demo) },
        )
    }
    composable<ScreenRoute.Settings> {
        SettingsScreen(onNavigateBack = { navController.popBackStack() })
    }
}
```

### 2. Unidirectional Data Flow & State Modeling

ViewModels expose an immutable `StateFlow<UiState>` and accept intents through direct functions:

```kotlin
@HiltViewModel
class DemoViewModel @Inject constructor(
    private val getWeatherDataUseCase: GetWeatherDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemoUiState())
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getWeatherDataUseCase()) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, data = result.data) }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingMessages = it.pendingMessages + PendingMessage(result.message)
                    )
                }
            }
        }
    }

    fun onMessageHandled(messageId: Long) {
        _uiState.update { state ->
            state.copy(pendingMessages = state.pendingMessages.filterNot { it.id == messageId })
        }
    }
}
```

In Compose screens:

```kotlin
@Composable
fun DemoScreen(
    viewModel: DemoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle transient snackbars safely
    state.pendingMessages.firstOrNull()?.let { message ->
        LaunchedEffect(message.id) {
            snackbarHostState.showSnackbar(message.text)
            viewModel.onMessageHandled(message.id)
        }
    }

    AsyncContent(
        isLoading = state.isLoading,
        isEmpty = state.data == null,
        onRefresh = viewModel::onRefresh,
    ) {
        WeatherContent(data = state.data!!)
    }
}
```

### 3. Offline-First Room Caching

Repositories expose a single source of truth from Room, refreshing from network transparently:

```kotlin
class DemoRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val dao: WeatherDao,
    private val dispatchers: AppDispatchers,
) : DemoRepository {

    override fun observeWeather(): Flow<WeatherEntity?> =
        dao.observeWeather().flowOn(dispatchers.io)

    override suspend fun refreshWeather(): ApiResult<Unit> = withContext(dispatchers.io) {
        when (val response = api.fetchWeather()) {
            is ApiResult.Success -> {
                dao.insertWeather(response.data.toEntity())
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> ApiResult.Error(response.cause)
        }
    }
}
```

---

## 🧪 Verification & Quality Commands

```bash
# Build APKs
./gradlew assembleDebug           # Build Debug APK
./gradlew assembleRelease         # Build Release APK

# Complete Quality Gate (Tests, KtLint, Detekt, Kover, Android Lint, Metalava)
./gradlew check

# Auto-format Kotlin source code
./gradlew ktlintFormat

# Static Code Analysis
./gradlew detekt

# Update Public API tracking signatures (Metalava)
./gradlew :core:apiDump :core:ui:apiDump

# Run Baseline Profile Generation
./gradlew :baselineprofile:generateBaselineProfile
```

---

## 📚 Documentation

For in-depth guides and architectural guidelines:
- [ARCHITECTURE.md](docs/ARCHITECTURE.md) — Layering, state management, and dependency guidelines.
- [CORE_MODULES.md](docs/CORE_MODULES.md) — Internal organization and contracts of `:core` and `:core:ui`.
- [FEATURE_TEMPLATE.md](docs/FEATURE_TEMPLATE.md) — Step-by-step guide for implementing new features.
- [DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md) — Material 3 tokens, typography, and custom UI components.
- [STANDARD.md](docs/STANDARD.md) — Kotlin coding conventions and best practices.
- [GIT_FLOW.md](docs/GIT_FLOW.md) — Branching rules and PR process.

---

## 📄 License

Licensed under the [MIT License](LICENSE).
