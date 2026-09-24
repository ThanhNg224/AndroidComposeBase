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

Dependabot auto-merge requires remote setup. Protect `main` with classic branch protection and require the `check` status, enable repository auto-merge, and add a fine-grained Actions secret named `BRANCH_PROTECTION_READ_TOKEN` with repository Administration:read. The metadata workflow uses `pull_request_target` only to read Dependabot metadata with the restricted `GITHUB_TOKEN`; it does not check out the PR or use user-managed secrets. It uploads only the eligibility decision, PR number, and PR head SHA. The separate `workflow_run` workflow is read from the default branch, validates the exact successful source run and artifact, then re-reads the current PR and checks its author, base, state, and head SHA before enabling auto-merge. It uses `GITHUB_TOKEN` with only `contents:write` and `pull-requests:write` for that operation. Ensure repository or organization Actions policy permits those workflow token permissions. For public repositories affected by GitHub's default event policy, explicitly allow `pull_request_target` for this workflow ([event policy guidance](https://docs.github.com/en/actions/reference/security/securely-using-pull_request_target)).

Only patch and minor updates are eligible; pull requests are never auto-approved. The privileged workflow fails closed if the artifact is malformed or stale, source run is not the expected workflow, required configuration is missing, or classic branch protection does not require `check`. Remote settings and workflow execution have not been verified for this repository; local YAML or shell validation cannot establish that they work remotely.
