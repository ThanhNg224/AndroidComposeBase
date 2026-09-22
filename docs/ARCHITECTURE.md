# ARCHITECTURE.md

## Purpose  
This document defines the architectural principles for Android projects using Clean Architecture and MVVM. It focuses on maintainability, scalability, modularity, and separation of concerns to ensure robust and adaptable applications.

## Architecture Goals  
- Maintainability  
- Scalability  
- Testability  
- Predictability  
- Low coupling  
- High cohesion  

## High Level Architecture  
The architecture is divided into three layers:  
**Presentation**  
↓  
**Domain**  
↓  
**Data**  

Dependencies always point inward, from Presentation to Domain to Data.

## Layer Responsibilities  

### Presentation  
Responsibilities:  
- Render UI  
- Observe state  
- Handle user interaction  
- Navigation  

Must not:  
- Access APIs  
- Access database  
- Contain business rules  

### Domain  
Responsibilities:  
- Business rules  
- UseCases  
- Entities  
- Repository interfaces  

Must not:  
- Depend on Android framework  
- Know networking/database details  

### Data  
Responsibilities:  
- Repository implementations  
- Remote data  
- Local data  
- Caching  
- Data synchronization  

Must not:  
- Contain UI logic  
- Leak API or database models to Presentation  

## Dependency Rule  
Dependencies always flow from Presentation → Domain → Data. Reverse dependencies are strictly prohibited to maintain clear separation and independence.

## Dependency Matrix  
| From / To         | Presentation | Domain | Data | Feature | Shared/Core |
|-------------------|--------------|--------|------|---------|-------------|
| Presentation      | -            | ✅     | ❌   | ❌      | ✅          |
| Domain            | ❌           | -      | ❌   | ❌      | ✅          |
| Data              | ❌           | ✅*    | -    | ❌      | ✅          |
| Feature           | ❌           | ❌     | ❌   | ❌      | ✅          |
| Shared/Core       | ❌           | ❌     | ❌   | ❌      | -           |

*Data → Domain dependency only allowed for Repository interface usage.

## Feature, Screen, and Flow

A **feature** is a vertical slice that owns one user-facing capability or bounded business context, such as authentication, profile, or checkout. It owns the presentation, domain, and data code required for that capability. A feature is not a synonym for one Activity, Fragment, or layout.

A **screen** is one concrete presentation destination within a feature: an Activity, Fragment, dialog destination, or equivalent navigable UI state. A screen owns its ViewModel and its `UiState`/`UiEvent` (plus any `pendingMessages` queue for transient one-shot requests), because those types describe that screen's presentation contract.

A **flow** is a user journey through one or more screens, for example Login → OTP verification → password reset. A flow does not introduce another package layer. Its screens remain owned by their feature; when a journey crosses feature boundaries, communicate through navigation and stable public contracts rather than direct feature dependencies.

The app uses a single **app shell** for its equally important top-level areas. `MainActivity` hosts `AppRoot` using Jetpack Navigation Compose (`NavHost`) and `FloatingNavBar`; Home, Demo, and Design System are Composable destinations routed via type-safe `ScreenRoute`. A bottom-navigation item represents a top-level destination, not a feature package. Secondary utilities such as Settings are opened from the app bar or profile actions. Do not add Settings, dialogs, or every screen in a feature to the bottom bar.

Each feature should own:

- Its screens and feature-specific presentation code.
- Its UseCases and repository interface usage.
- Its data implementation when real persistence or remote data is needed.

Avoid coupling between features to promote modularity and independent development. Do not create a root-level `screens/` package: it separates a screen from the capability, state, and use cases it belongs to, which makes ownership less clear.

## Recommended Feature Structure

For a feature with one screen, keep the presentation packages flat. `sample/demo` uses this shape and avoids speculative nesting; product capabilities belong under `feature/<feature-name>/`.

```
feature/<feature-name>/
    presentation/
        ui/
        viewmodel/
        state/
    domain/
        model/
        repository/
        usecase/
    data/
        datasource/
        repository/
        mapper/
```

When a feature grows to two or more screens, group the presentation code by screen while keeping domain and data at the feature level:

```
feature/auth/
    presentation/
        login/
            LoginScreen.kt
            LoginViewModel.kt
            LoginUiState.kt
            LoginUiEvent.kt
        otp/
            OtpScreen.kt
            OtpViewModel.kt
            OtpUiState.kt
            OtpUiEvent.kt
        components/                 # only when shared by two or more auth screens
    domain/
        repository/
        usecase/
    data/
        datasource/
        repository/
        mapper/
```

Do not create empty `domain/`, `data/`, `components/`, or per-screen packages merely to match this example. If a second screen has a genuinely separate business capability and does not share domain/data ownership, model it as a separate feature instead. The exact shape may vary, but ownership and dependency rules must remain consistent.

## Repository Pattern  
Repositories coordinate multiple data sources to provide a unified interface. Repository interfaces belong to the Domain layer, while their implementations reside in the Data layer.

## Data Sources  
Data sources include:  
- RemoteDataSource  
- LocalDataSource  
- Optional CacheDataSource  

Repositories orchestrate these data sources to provide consistent data.

## UseCase Guidelines  
- One business capability per UseCase  
- Composable and reusable  
- Independent of framework and UI  
- Easily testable  

## Mapper Guidelines  
Mapping flow:  
- ApiModel → Entity → UiModel  
- DatabaseModel → Entity  

Mappers should be deterministic and maintain clear transformations between layers.

## MVVM Responsibilities  
**View:**  
- Render UI only  

**ViewModel:**  
- Own screen state  
- Coordinate UseCases  
- Contain no Android framework business logic  

## Data Flow  
Recommended request flow:  
User Action → View → ViewModel → UseCase → Repository → DataSource → API/Database.  
The response flows back in reverse order after appropriate mapping at each layer, ensuring data consistency and separation of concerns.

## UI State  
Use a clear separation of:  
- UiState (screen state, including any `pendingMessages` queue for transient one-shot requests)
- UiEvent (user or system events)  

A transient one-shot request (snackbar action, navigation) is a field on `UiState` itself —
acknowledged once shown — not a separate `Channel`-backed effect type. A `Channel` can silently
drop or re-fire an emission across a configuration change; a `StateFlow` field cannot.

## Dependency Injection  
- Use constructor injection  
- Inject abstractions, not implementations  
- Keep modules independent and loosely coupled  

## Shared Modules  
- Move code into shared/core modules only after proven reuse  
- Avoid feature-specific logic in shared modules to maintain modularity  

## Shared Module Rules  
- Shared code must be framework-agnostic where possible  
- Never depend on feature modules  
- Avoid business logic tied to a single feature  
- Prefer moving code only after two or more proven reuse cases  

## Feature Communication  
- Communicate through public contracts/interfaces  
- Avoid direct dependencies between features  

## Error Propagation  
- Errors propagate from Data → Domain → Presentation  
- Convert technical errors into domain-friendly results  
- UI presents user-friendly error messages  

## Architecture Smells  
- Business logic in UI  
- ViewModel accessing APIs directly  
- Domain layer depending on Android framework  
- Circular dependencies between modules  
- God repositories handling too many responsibilities  
- Massive ViewModels  
- Duplicate business logic scattered across layers  
- Shared modules depending on feature-specific code  

## Refactoring Strategy  
Improve architecture incrementally by making small, safe refactors that preserve existing behavior. Avoid big-bang rewrites to reduce risk and maintain project stability. Prioritize continuous improvement and maintainability through gradual enhancements.

## Architecture Principles  
- **Separation of Concerns:** Each layer and module has a distinct responsibility to reduce complexity.  
- **Dependency Inversion:** High-level modules should not depend on low-level modules; both depend on abstractions.  
- **Single Responsibility:** Classes and modules should have one reason to change, focusing on a single task.  
- **Feature Isolation:** Features should be self-contained to enable independent development and testing.  
- **Composition over Inheritance:** Prefer composing behaviors over complex inheritance hierarchies for flexibility.  
- **Explicit Dependencies:** Dependencies should be clearly declared and injected to improve testability and clarity.  
- **Predictable Data Flow:** Data should flow in a clear, unidirectional manner to simplify reasoning and debugging.  
- **Stable Public Contracts:** Interfaces between modules should be stable and well-defined to minimize coupling.  

## Architecture Review Checklist  
- [ ] Are layer boundaries clearly defined and respected?  
- [ ] Do dependencies flow inward only (Presentation → Domain → Data)?  
- [ ] Is business logic contained exclusively in the Domain layer?  
- [ ] Are UI components free of business and data access logic?  
- [ ] Are repository interfaces defined in Domain and implementations in Data?  
- [ ] Are UseCases focused on a single business capability?  
- [ ] Are mappers deterministic and correctly transforming data between layers?  
- [ ] Is dependency injection used consistently with abstractions?  
- [ ] Are shared modules free from feature-specific logic?  
- [ ] Is feature communication handled via public contracts without tight coupling?  
- [ ] Is error handling consistent and user-friendly across layers?  
- [ ] Are ViewModels free from Android framework dependencies and business logic?  
- [ ] Is UI state management separated into UiState and UiEvent, with transient requests modeled as acknowledged state rather than a `Channel`-backed effect?  
- [ ] Are data sources properly encapsulated and orchestrated by repositories?  
- [ ] Are modules designed for low coupling and high cohesion?  
- [ ] Are there no circular dependencies between modules or layers?  
- [ ] Is the architecture scalable and maintainable for future growth?  
- [ ] Are tests easily written for UseCases, repositories, and ViewModels?

### Current Package Layout

Everything above this section describes the target architecture. The folders below represent the current structure in the codebase:

The base is partitioned into `:core` (headless foundation) and `:core:ui` (Material 3 Compose design system), both published to JitPack. `:app` owns the Hilt DI graph, database, type-safe navigation, and Compose feature screens.

```
app/src/main/java/com/thanhng224/androidcomposebase/
  MainActivity.kt                            # single-Activity shell: extends BaseComposeActivity, hosts AppRoot
  AndroidComposeBaseApplication.kt           # application entry point: Timber logging, bounded startup
  di/                                        # app-owned Hilt modules (AppCoreModule, NetworkModule, DatabaseModule)
  startup/AppStartupCoordinator.kt           # bounded theme-apply-at-startup
  navigation/
    ScreenRoute.kt                           # type-safe route definitions (@Serializable)
    AppNavHost.kt                            # Navigation Compose NavHost
  appshell/
    AppRoot.kt                               # root composable: Scaffold, FloatingNavBar, snackbar host
    AppViewModel.kt                          # global app shell state & theme/navigation observation
    NavItem.kt                               # bottom navigation item definition
    HomeScreen.kt                            # landing home destination
  database/                                  # Room offline-first database
    AppDatabase.kt                           # RoomDatabase definition
    WeatherDao.kt                            # DAO for weather cache
    WeatherEntity.kt                         # cached entity
    RoomTypeConverters.kt                    # serialization converters
  feature/
    settings/
      domain/
        repository/SettingsRepository.kt
        usecase/ObserveThemeUseCase.kt, SetThemeUseCase.kt, GetCurrentLanguageUseCase.kt, SetLanguageUseCase.kt
      data/
        repository/SettingsRepositoryImpl.kt # persists language via SettingsStore, applies via LocaleManager
      presentation/
        state/SettingsUiState.kt, SettingsUiEvent.kt, PendingSettingsMessage.kt
        viewmodel/SettingsViewModel.kt
        ui/SettingsScreen.kt                 # pure Composable settings screen
      di/SettingsModule.kt
    demo/
      domain/
        repository/DemoRepository.kt
        usecase/IncrementCounterUseCase.kt, ObserveDemoCountUseCase.kt, SaveDemoCountUseCase.kt,
          FetchDemoWeatherUseCase.kt, ObserveWeatherUseCase.kt, RefreshWeatherUseCase.kt
      data/
        repository/DemoRepositoryImpl.kt     # Room DAO + Retrofit API offline-first implementation
        dto/DemoWeatherResponseDto.kt
        datasource/DemoApiService.kt, DemoRemoteDataSource.kt (+ DemoRemoteDataSourceImpl)
        mapper/DemoWeatherMapper.kt
      presentation/
        state/DemoUiState.kt, DemoUiEvent.kt, PendingDemoMessage.kt
        viewmodel/DemoViewModel.kt
        ui/DemoScreen.kt                     # pure Composable demo & weather screen
      di/DemoModule.kt
    designsystem/
      presentation/
        state/DesignSystemUiState.kt, DesignSystemUiEvent.kt
        viewmodel/DesignSystemViewModel.kt
        ui/DesignSystemScreen.kt             # showcases FloatingNavBar, AppDialog, AsyncContent, AppButton, AppTopBar
    onboarding/
      presentation/ui/OnboardingScreen.kt    # onboarding flow
    login/
      presentation/ui/LoginScreen.kt         # authentication screen
```

`feature/settings` is the canonical product feature. `SettingsScreen` renders theme and language as interactive setting rows with Material 3 selection dialogs (`AppDialog`). Selecting a language persists through `SettingsRepository` first, then the app-owned `LocaleManager` applies it via `AppCompatDelegate`.

`feature/demo` represents the offline-first data and network reference: its counter persists through `DemoRepositoryImpl` backed by `SettingsStore`, while weather data is persisted in Room (`AppDatabase`) and refreshed from network (`DemoApiService`) with fallback to cache when offline.

`MainActivity` extends `core/ui/base/BaseComposeActivity` and calls `setContent` to host `AppRoot`. Inside `AppRoot`, `AppNavHost` coordinates navigation between destinations using AndroidX Navigation Compose and type-safe `ScreenRoute`. Floating navigation is handled smoothly by `FloatingNavBar`.
