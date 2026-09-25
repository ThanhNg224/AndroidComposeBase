# Engineering Guidelines

This repository is an AndroidComposeBase template. Its current modules are `:app`, `:core`, `:core:ui`, and `:baselineprofile`. Improve the reusable foundation and the sample app without adding modules or abstractions that do not have a demonstrated reuse boundary.

## Before changing code

1. Read this file and the relevant sections of [ARCHITECTURE.md](docs/ARCHITECTURE.md), [STANDARD.md](docs/STANDARD.md), and [GIT_FLOW.md](docs/GIT_FLOW.md).
2. Inspect the implementation, its callers, tests, and similar patterns. Confirm current symbols and API behavior instead of relying on an older design document.
3. Prefer the smallest complete change that preserves established behavior. Breaking changes to `:core` or `:core:ui` public APIs are allowed while no real downstream consumer exists and no API freeze has been requested; review the `apiDump` diff and run the matching `apiCheck` for every public API change.

## Work planning

- Do not create a worktree or temporary branch unless the user explicitly requests it. Keep small changes in the current checkout.
- For a multi-file architecture change or three or more independent tasks, settle the scope first, then split it into verifiable tasks for implementers. The coordinating agent reviews each diff and runs its checks before continuing; use a reviewer for the final cross-cutting pass.
- Do not add unit tests that only check Compose layout or styling. Test behavior, state, and accessibility contracts when needed.
- Ask the user when repository evidence and the agreed scope do not resolve an architectural choice. Preserve other contributors' working-tree edits.

## Architecture

- Within a feature, presentation depends on domain contracts; data implements those contracts. Dependencies do not point from domain to data or presentation.
- `:app` is the composition root for Hilt, navigation, features, and app-specific infrastructure. `:core` and `:core:ui` remain independent of app features.
- `presentation/AppRoot.kt` gates startup and onboarding; `presentation/MainShell.kt` assembles Navigation 3 entries and owns the floating top-level navigation. Features declare their own routes, entries, and top-level destination metadata. Keep destination labels localized.
- Keep business rules out of Composables and Android framework types out of domain contracts.
- Add a use case or shared abstraction when it expresses a real capability or removes repeated policy, not as a pass-through layer by default.
- Keep app-specific code under its feature. Promote code to `:core` only when it is genuinely reusable.

## Implementation and verification

- Keep state immutable where practical and coroutines structured. Propagate cancellation; handle expected failures explicitly.
- Never log credentials, personal data, request payloads, or secrets. Avoid noisy release logs.
- Add tests for business logic, state transitions, persistence boundaries, and concurrency cases. Do not add unit tests that only mirror a Compose layout.
- Run focused checks for the changed code, then the relevant project gates. Report the exact commands and results; distinguish local checks from CI, device, and release evidence.
- Keep documentation aligned with code and remove temporary plans or notes when the work is complete.

## Collaboration

- Keep changes focused and reviewable. Preserve other contributors' edits in the shared working tree.
- Use the repository's `main`-based workflow and conventional commit prefixes described in [GIT_FLOW.md](docs/GIT_FLOW.md).
- Do not publish, tag, or push as part of a local implementation unless explicitly requested.
