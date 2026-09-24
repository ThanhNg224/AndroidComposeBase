# Architecture

AndroidComposeBase uses feature-oriented Clean Architecture inside the existing Gradle modules. Keep boundaries explicit without creating modules or layers that add no independent responsibility.

## Modules

| Module | Owns |
|---|---|
| `:app` | Application entry point, Hilt composition root, navigation, app shell, and feature implementations. |
| `:core` | Reusable Android foundation and platform adapters; it has no dependency on app features. |
| `:core:ui` | Reusable Compose theme and UI components. |
| `:baselineprofile` | The sample app's macrobenchmark and baseline-profile journey. |

The app assembles implementations and dependencies. A feature may use `:core` or `:core:ui`; shared modules must not depend on `:app` or one of its features.

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

Place a capability under `app/src/main/java/<package>/feature/<name>/`. A small feature can keep `presentation/ui`, `presentation/viewmodel`, and `presentation/state` flat. Add `domain/` or `data/` only when the feature has real policy or an external data boundary.

```text
feature/<name>/
  presentation/       # Composable, ViewModel, UiState, UiEvent
  domain/             # feature contracts and meaningful business rules
  data/               # contract implementations, sources, and mapping
  di/                 # feature bindings/providers registered by :app
```

The sample includes `feature/onboarding` and `feature/settings`; `sample/demo` demonstrates Room-backed offline weather and counter state, while `sample/designsystem` showcases `:core:ui`. These samples are not required layers for new product features.

Keep one screen's UI, ViewModel, and state together. When multiple screens share a capability, group their presentation code by screen and share feature-level domain/data code. Split independent capabilities into separate features instead of grouping them only because they are navigated together.

## Presentation and state

- Composables render state, forward user input, and collect `StateFlow` with lifecycle awareness. They do not access repositories, Room, or APIs.
- ViewModels own screen state and coordinate feature contracts. Keep Android resource resolution at the presentation edge.
- Model durable screen state as immutable `UiState`. For messages that must survive recreation until acknowledged, store a small pending-message list in state and remove the matching head after display. Use a transient event mechanism only when its loss/replay behavior is intentional.
- Navigation is declared in `navigation/ScreenRoute.kt` and assembled in `presentation/AppRoot.kt`. Keep route changes at the app boundary.

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
5. Update API snapshots only for intentional public `:core` or `:core:ui` API changes.
