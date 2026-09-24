# Repository Guide

Read [AGENTS.md](AGENTS.md) and the relevant project documents before editing. AndroidComposeBase currently contains `:app`, `:core`, `:core:ui`, and `:baselineprofile`; `:app` is the composition root and owns feature implementations.

## Architecture

```text
Presentation -> Domain contract <- Data implementation
                       ^
                       |
                :app composition root
```

Use Composables for rendering and input, ViewModels for presentation state, domain contracts for feature policy, and data implementations for persistence/networking. Keep `:core` free of feature dependencies. Avoid pass-through use cases and speculative modules.

## Common checks

```bash
./gradlew check
./gradlew :app:assembleRelease
./gradlew :core:apiCheck :core:ui:apiCheck
python3 -m unittest discover -s scripts -p 'test_*.py' --verbose
python3 scripts/smoke_init_project.py --build full-clean
./scripts/verify-publication.sh --release
```

The library source version is `0.1.0`; no remote publication is implied. Follow [GIT_FLOW.md](docs/GIT_FLOW.md) for local commits and pull requests. Do not claim remote CI, device, or performance results without running those checks.
