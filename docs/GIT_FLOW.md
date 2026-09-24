# Git Workflow

`main` is the repository's primary branch. Use short-lived topic branches for pull requests when working in a shared remote workflow. Local template work may use the current checkout when the task explicitly requests it.

## Commits

Use a conventional prefix and a concise imperative subject:

- `feat`: user-facing capability
- `fix`: correctness issue
- `refactor`: behavior-preserving structure change
- `docs`: documentation or rules
- `test`: tests only
- `build` / `chore` / `perf`: build, maintenance, or measured performance work

Keep each commit focused. Do not include generated output, credentials, unrelated cleanup, or temporary plans.

## Pull requests

Keep each PR reviewable and describe behavior changes, architecture/API impact, and the checks actually run. Resolve review feedback before merging. Branch protection and required checks are configured in the repository settings; this document does not claim those remote settings are enabled.

Before requesting review, run the checks that apply:

```bash
./gradlew check
./gradlew :app:assembleRelease
./gradlew :core:apiCheck :core:ui:apiCheck
python3 -m unittest discover -s scripts -p 'test_*.py' --verbose
python3 scripts/smoke_init_project.py --build full-clean
./scripts/verify-publication.sh --release
```

For a docs-only or narrowly scoped change, use judgment and report any skipped gate with the reason. A local run cannot establish remote workflow success.

## Release and dependency automation

Library source version is maintained in `core/gradle.properties`; app version is in `app/build.gradle.kts`. A version change does not publish an artifact. Release tags and publication require a separately reviewed release action.

Dependabot auto-merge requires remote setup: protect `main` with the `check` required status, enable repository auto-merge, and configure `BRANCH_PROTECTION_READ_TOKEN` with permission to read repository administration settings. The workflow fails closed when required configuration is unavailable. It does not auto-approve pull requests.
