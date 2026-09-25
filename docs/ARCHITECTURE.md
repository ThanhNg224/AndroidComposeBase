# Architecture

AndroidComposeBase uses feature-oriented Clean Architecture inside the existing Gradle modules. Keep boundaries explicit without creating modules or layers that add no independent responsibility.

## Modules

| Module | Owns |
|---|---|
| `:app` | Application entry point, Hilt composition root, navigation, app shell, and feature implementations. |
| `:core` | Reusable Android foundation and platform adapters; it has no dependency on app features. |
| `:core:ui` | Reusable Compose theme and UI components. |
| `:baselineprofile` | The sample app's macrobenchmark and baseline-profile journey. |

The app assembles implementations and dependencies. A feature may use `:core` or `:core:ui`; shared modules must not depend on `:app` or one of its features. `AppRoot` applies the ready-state theme and gates onboarding before showing `MainShell`. `MainShell` assembles feature-owned Navigation 3 entries and top-level destinations, displays them with a floating navigation bar, and keeps a separate back stack for each tab so returning to a tab preserves its navigation state.

## Feature dependency direction

```text
Presentation -> Domain contract <- Data implementation
                         ^
                         |
                  :app composition root
```

Presentation calls domain contracts and maps their values into UI state. Data implements repository contracts and owns API, database, and platform integration. The app's DI graph binds the implementation to its contract. Domain types stay independent of Android resources and concrete data details.

An optional runtime flow is:

```text
Composable -> ViewModel -> use case (when useful) -> repository contract
                                                     <- repository implementation -> source
```

The compile-time dependency is presentation to domain, and data to domain. Data does not depend on presentation. `:app` is where both implementations are composed at runtime.

## Feature ownership

Place a capability under `app/src/main/java/<package>/feature/<name>/`. Keep its route, entry registration, UI, ViewModel, and state together. Add `domain/` or `data/` when the feature has a real contract or external data boundary, and `di/` when it owns app-graph bindings.

```text
feature/<name>/
  navigation/         # serializable NavKey route, entry, optional top-level metadata
  presentation/
    ui/                # Composable destination
    viewmodel/         # screen state and action coordination
    state/             # immutable UiState and UiEvent
  domain/             # feature contracts and meaningful business rules
  data/               # contract implementations, sources, and mapping
  di/                 # feature bindings/providers registered by :app
```

The sample includes `feature/onboarding` and `feature/settings`; `sample/demo` demonstrates Room-backed offline weather and counter state, while `sample/designsystem` showcases `:core:ui`. These samples are not required layers for new product features.

Declare each route as a serializable Navigation 3 key, for example `@Serializable data object ProfileRoute : NavKey`, and expose an `EntryProviderScope<NavKey>.profileEntry()` registration from the feature's `navigation/` package. A top-level destination also exposes `TopLevelDestination` metadata with a stable unique ID. `presentation/MainShell.kt` registers feature entries and lists top-level destinations in display order; features do not depend on the app shell. Keep one screen's UI, ViewModel, and state together. When multiple screens share a capability, group their presentation code by screen and share feature-level domain/data code. Split independent capabilities into separate features instead of grouping them only because they are navigated together.

## Presentation and state

- Composables render state, forward user input, and collect `StateFlow` with lifecycle awareness. They do not access repositories, Room, or APIs.
- ViewModels own screen state and coordinate feature contracts. Keep Android resource resolution at the presentation edge.
- Model durable screen state as immutable `UiState`. For messages that must survive recreation until acknowledged, store a small pending-message list in state and remove the matching head after display. Use a transient event mechanism only when its loss/replay behavior is intentional.
- Each feature declares its Navigation 3 route key and `EntryProviderScope<NavKey>` entry in `navigation/`; top-level features also declare their `TopLevelDestination` metadata. `presentation/MainShell.kt` lists top-level destinations in display order and assembles feature entry registrations. Each top-level tab has a separate back stack, preserving its route state when switching tabs and restoring after process recreation. Keep cross-feature navigation assembly and localized destination labels at the app boundary.

## Domain and use cases

Repository contracts live in the feature's domain package when presentation needs a stable boundary over data or platform operations. Contracts return domain values, not Retrofit DTOs, Room entities, Android `Context`, or resource IDs.

Use a use case for a named business capability, orchestration reused by multiple callers, or a rule that benefits from isolated tests. A one-line pass-through is optional; a ViewModel may call a clear repository contract directly when no meaningful policy is added.

## Data and offline behavior

Data implementations coordinate local and remote sources, map persistence/wire models, and expose domain-friendly values. For the weather example, Room is the read source of truth: UI observes a `Flow`, refresh writes successful responses to Room, and failures preserve the last cached data. Schema changes require explicit Room migrations; destructive fallback is disabled.

## Change checklist

1. Confirm the capability belongs to an existing feature or shared module.
2. Define only the boundary needed for independent policy or external data access.
3. Bind implementations in `:app`, not in reusable `:core` modules.
4. Test business rules, persistence ordering, failures, and races at the layer that owns them.
5. Review API dumps and update snapshots for intentional public `:core` or `:core:ui` API changes. Breaking changes are allowed before real downstream consumers exist when no API freeze was requested; always inspect the `apiDump` diff and run the corresponding `apiCheck`.
