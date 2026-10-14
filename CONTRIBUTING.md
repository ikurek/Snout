# Contributing to Snout

Thanks for helping. Bug reports, fixes and features are all welcome; for anything larger than a fix, open an
issue first so we can agree on the approach before you spend time on it.

## Before you start

- Read the [Architecture](README.md#architecture) and [Design system](README.md#design-system) sections of the
  README. New code should follow the conventions there: layers and DI, one `uiState` per view model, the
  `XUiState.kt` layout, handler naming, design-system components and `Spacing` tokens instead of raw values.
- Snout talks only to the PostHog instance the user configures. Don't add analytics, crash reporting or any other
  service that sends data elsewhere.
- Never commit API keys or other secrets, including in test fixtures and screenshots.

## Pull requests

- Keep each pull request to one change, with focused commits whose subject says what changed in the imperative
  ("Add a severity filter to logs").
- Run the checks listed under [Build](README.md#build) and make sure they pass. They include the iOS compile:
  the Android tasks don't compile Kotlin/Native, so iOS-only breakage only shows up there.
- UI changes: update the screenshot references as the Build section describes and commit them, so reviewers see
  the visual change in the diff.
- Add or update tests for logic you change: mappers and repositories in `shared/data`, use cases in
  `shared/domain/usecase`, view models in `composeApp/src/commonTest`.
- Add yourself to [AUTHORS](AUTHORS) in your first pull request.

## License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE), the
license of this project.
