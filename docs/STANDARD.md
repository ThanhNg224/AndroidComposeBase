# Kotlin and Android Standards

These standards apply to Kotlin code in `:app`, `:core`, and `:core:ui`. Follow the surrounding code and automated formatters; keep changes small and reviewable.

## Kotlin

- Prefer `val`, immutable state, explicit names, and small cohesive functions.
- Use sealed types for finite state/results when they make invalid states harder to express.
- Keep visibility as narrow as possible. The `:core` and `:core:ui` public APIs are tracked; update their API dumps only for intentional changes.
- Use constructor injection. Do not add a service locator or a global coroutine scope.
- Re-throw `CancellationException`; handle expected failures explicitly and preserve their useful context.
- Avoid pass-through use cases, catch-all utilities, and abstractions without demonstrated reuse.

## Coroutines and state

- Use structured scopes owned by the application, ViewModel, or operation.
- Expose immutable `StateFlow` to presentation; keep mutable flows private.
- Serialize or version competing asynchronous mutations when completion order could change the latest state.
- Represent user-visible failure in state or an explicit result. Do not silently swallow exceptions.

## Compose and Android

- Composables render state and emit user actions; they do not own business rules or call data sources.
- Hoist reusable component state and expose a `Modifier` on public composables where appropriate.
- Keep strings and dimensions in resources/tokens. Preserve accessibility semantics and Android touch targets of at least 48dp.
- Handle lifecycle and window insets through the existing AndroidX/Compose APIs used by the project.
- Keep platform types at presentation or data boundaries; domain contracts should use Kotlin values.

## Persistence and networking

- Persist durable app data through feature-owned contracts and implementations.
- Use Room as the sample weather read source of truth; add an explicit migration whenever its schema changes.
- Keep API and database models inside data code and map them before exposing values to presentation.
- Validate external input and never log credentials, personal data, tokens, payloads, or full sensitive URLs.

## Testing and quality

- Test behavior: domain rules, state transitions, ordering, errors, persistence, and concurrency.
- Keep UI-only layout tests out unless a behavior or accessibility contract needs verification.
- Run the relevant Gradle checks (`check`, API checks, release assembly, publication consumer) and report actual command output. A local result is not evidence of remote CI or device behavior.
- Remove stale comments, dead code, and temporary notes as part of the change.
