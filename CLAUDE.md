# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project State (read this first)

This repo is **AndroidComposeBase**: a modern, production-ready Android base starter built **100% with Jetpack Compose**, **Material 3 Design System**, **Offline-First Room Caching**, **Type-Safe Navigation**, **Coroutines & StateFlow**, and **Clean Architecture**.

Modules:
- `:core`: Reusable headless foundation (network, secure store, settings DataStore, localization, dispatchers). DI-agnostic, published to JitPack.
- `:core:ui`: Jetpack Compose Design System (Material 3 Theme, Color Schemes, Typography, Shapes, Dimens, and reusable components: `FloatingNavBar`, `AppDialog`, `AsyncContent`, `AppButton`, `AppTopBar`, `UiTextExtensions`). DI-agnostic, published to JitPack.
- `:app`: Application shell, type-safe navigation with Kotlinx Serialization, Hilt DI graph, offline-first Room database (`AppDatabase`), and feature screens.
- `:baselineprofile`: Macrobenchmark & Baseline Profile generation for cold startup and smooth Compose scroll/render performance.

## Commands

```bash
./gradlew :app:assembleDebug        # build debug APK
./gradlew :app:assembleRelease      # build release APK
./gradlew test                      # run JVM unit tests
./gradlew check                     # full local gate: unit tests, lint, ktlint, detekt, apiCheck, Kover
./gradlew ktlintFormat              # auto-format Kotlin where ktlint can safely fix
./gradlew detekt                    # static analysis
./gradlew :core:apiDump :core:ui:apiDump  # dump public API signatures (Metalava)
```

Quality gates are part of the base: Android lint (`abortOnError` on every module), ktlint, detekt, apiCheck, and Kover coverage are wired into `check`.

## Architecture & Layering

Compile dependency direction (both point inward, neither sideways):
```
Presentation -> Domain <- Data
```

Runtime call flow:
```
UI (Composable) -> ViewModel -> UseCase -> Repository interface -> RepositoryImpl -> DataSource / Room DAO / API
```

Layer rules:
- **Presentation**: renders Compose UI, observes state with `collectAsStateWithLifecycle()`, handles input/navigation. No API/DB access, no business rules.
- **Domain**: business rules, UseCases, entities, repository *interfaces*. Zero Android framework dependencies.
- **Data**: repository *implementations*, remote/local data sources, Room DAOs, mappers. No UI logic; never leak API/DB models to Presentation.

## UI State & Navigation Convention

- ViewModels are plain `androidx.lifecycle.ViewModel` exposing `StateFlow<UiState>`.
- Transient one-shot requests (snackbars) are modeled as a small `pendingMessages: List<PendingMessage>` field on that state, acknowledged via `onMessageHandled(id)` once shown — never a `Channel`-backed generic effect type, avoiding dropped or duplicated emissions across configuration changes.
- Navigation uses Jetpack Navigation Compose 2.8+ with type-safe `@Serializable` routes (`ScreenRoute`).
- Touch targets must be at least 48x48dp, adhering to the 8-point spacing scale in `Dimens`.
