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

Dependabot auto-merge requires remote setup. Protect `main` and require the `check` status, enable repository auto-merge, and add these fine-grained tokens under **Settings → Secrets and variables → Dependabot**:

- `BRANCH_PROTECTION_READ_TOKEN`: repository Administration:read, used only to verify auto-merge and branch protection settings.
- `DEPENDABOT_AUTOMERGE_TOKEN`: Contents:write and Pull requests:write, used to enable auto-merge.

The workflow runs on `pull_request`, does not check out or execute PR source, and uses the read-only `GITHUB_TOKEN` only to read Dependabot metadata. Dependabot pull request workflows cannot access Actions secrets, so both configured tokens must be Dependabot secrets. It permits patch and minor updates only, never auto-approves, and fails closed when a required secret, repository setting, or `check` branch protection requirement is missing. Remote settings and workflow execution have not been verified for this repository; local YAML or shell validation cannot establish that they work remotely.
