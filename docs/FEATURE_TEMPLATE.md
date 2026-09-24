# Adding a Feature

Features live under `app/src/main/java/<package>/feature/<name>/`. Start with the smallest structure that describes the capability; domain and data packages are optional until the feature needs those boundaries.

```text
feature/<name>/
  presentation/
    ui/             # Composable destination
    viewmodel/      # screen state and action coordination
    state/          # immutable UiState and user UiEvent
  domain/           # optional: feature contracts and business rules
  data/             # optional: persistence/network implementations
  di/               # optional: feature providers/bindings in the app graph
```

## Data-backed feature flow

```text
Composable -> ViewModel -> domain contract <- data implementation -> source
                  ^                 ^
                  |                 |
                 :app composes the dependencies
```

The presentation layer depends on a domain contract. Data implements it, and `:app` binds both at the composition root. Domain contracts use Kotlin values and must not expose Android resource IDs, `Context`, Room entities, or Retrofit DTOs.

Add a use case when it names a meaningful business operation, coordinates multiple contracts, is reused, or isolates a rule worth testing. For a simple screen, a ViewModel may depend directly on a focused domain repository contract.

## Reference examples

- `feature/onboarding` owns its completion contract and storage implementation. It persists success before the app shell navigates Home.
- `feature/settings` adapts `ThemeManager` and `LocaleManager`; its domain language contract uses string language tags while presentation maps configured tags to `AppLanguage` display resources.
- `sample/demo` demonstrates local persistence, Room as the weather read source of truth, and a remote refresh path.
- `sample/designsystem` has presentation only because it is a UI showcase without a data boundary.

Keep single-screen features flat. If multiple screens share one capability, group their UI and state by screen while retaining shared domain/data code at feature level. If the screens own independent capabilities, make separate features.

## Add and verify

1. Search for an existing contract, component, route, and similar feature before adding one.
2. Add only the state and boundaries needed by the feature.
3. Register app navigation in `navigation/ScreenRoute.kt` and `presentation/AppRoot.kt` when the screen is navigable.
4. Add behavior tests for business rules, state transitions, error cases, and asynchronous ordering. Do not add tests that only restate static Compose layout.
5. Run the affected tests, formatting/lint, and `./gradlew check` when appropriate.
