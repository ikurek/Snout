# Contributing to Snout

Thanks for helping. Bug reports, fixes, features and documentation are all welcome. By taking part you agree to
follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to help

- **Report a bug**: open an [issue](https://github.com/ikurek/snout/issues/new/choose) with the bug template. Say which platform, app version
  and PostHog instance type (US cloud, EU cloud or self-hosted) you use, and what you did, expected and got.
  Leave out API keys and anything from your project you don't want public.
- **Suggest a feature**: open an issue with the feature template and describe the problem it solves first.
- **Send a fix or feature**: for anything larger than a fix, open an issue first so we can agree on the approach
  before you spend time on it.
- **Security issues**: don't open a public issue; follow [SECURITY.md](SECURITY.md).

## Before you start

- Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). New code should follow the conventions there: layers and DI,
  one `uiState` per view model, the `XUiState.kt` layout, handler naming, design-system components and `Spacing`
  tokens instead of raw values.
- Snout talks only to the PostHog instance the user configures. Don't add analytics, crash reporting or any other
  service that sends data elsewhere.
- Never commit API keys or other secrets, including in test fixtures and screenshots.

## Building and checks

Android: `./gradlew :androidApp:assembleDebug`. iOS: open `iosApp` in Xcode (the framework is built by Gradle).
Checks before a pull request, the same ones the Quality gate workflow runs on every pull request:

```
./gradlew detekt lint                                   # static analysis
./gradlew :konsist:test                                 # architecture rules
./gradlew testAndroidHostTest                           # unit tests
./gradlew :androidApp:validateDebugScreenshotTest       # screenshot tests
./gradlew :composeApp:compileKotlinIosSimulatorArm64    # iOS compile
```

The Android tasks never compile Kotlin/Native, so the iOS compile is needed to catch iOS-only breakage. `detekt`
(with ktlint rules) only checks; `./gradlew detektFormat` fixes what the formatting rules can (it rewrites files,
so review the diff after it) and then `detekt` reports what is left.

Architecture rules: `konsist/` holds [Konsist](https://docs.konsist.lemonappdev.com) tests that read every module's
sources and check the layering and conventions in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): which layer may
import which, DTO, repository, use case and view model shapes, and previews. A failure names the rule and the
offending files. When a convention changes on purpose, change its rule in the same pull request.

Screenshot tests: every public `*Preview()` composable in `composeApp` is rendered by generated wrappers in
`androidApp/src/screenshotTest` (Compose Preview Screenshot Testing): screens (previews under `features/`) in light
and dark mode on a phone-sized device, design-system previews (`designsystem/`) in light mode only, since their dark
colours come from the same theme the screens' dark shots cover. Wrappers are
grouped per preview package, so the reference images under `androidApp/src/screenshotTestDebug/reference`
mirror the package tree. Previews are `public` only because those wrappers live in another module and call
them; they are not API for anything else. Verify with `./gradlew :androidApp:validateDebugScreenshotTest`. After adding or
renaming previews run `scripts/generate_screenshot_tests.py`; after an intended UI change regenerate the
references with `./gradlew :androidApp:updateDebugScreenshotTest` and commit them.

Open-source licenses: the [AboutLibraries](https://github.com/mikepenz/AboutLibraries) plugin in `composeApp` runs
`exportLibraryDefinitions` as part of every build. It collects the non-test classpaths of `composeApp` (Android
runtime plus the common and iOS metadata, so everything `:shared` pulls in is listed) into
`composeApp/build/generated/aboutLibraries/composeResources/files/aboutlibraries.json`, which is the
`androidMain` and `iosMain` compose resource directory, so both apps (including the Xcode-run Gradle build) bundle
it without a manual step; it is not committed. The export runs offline: license texts come from
`composeApp/aboutlibraries/licenses/`, and only the licenses in `aboutLibraries.license.allowedLicenses` are
accepted. A dependency bringing a new license fails the build; add its text there (same JSON shape, `hash` = SPDX id)
and allow it. `./gradlew :composeApp:findLibraries` lists what is collected.

If Gradle cannot find the Android SDK, add a `local.properties` with `sdk.dir=/path/to/Android/sdk`.

## Releasing

Maintainers release by pushing a `major.minor.patch` tag (`1.2.0`, no `v`) on `master`. Codemagic
(`codemagic.yaml`) builds both apps from it, uploads them to the Google Play internal track and TestFlight, and
publishes a GitHub release with the APK and the commits since the previous tag. Promote a build to production in the
Play Console and App Store Connect.

The tag is the only version: the Android `versionName` and iOS `MARKETING_VERSION` are the tag, and both version codes
are `major * 10000 + minor * 100 + patch` (1.2.3 is 10203), so minor and patch stay under 100. Local builds default to
0.0.1. Release builds are shrunk with R8; a signed local release build needs the upload key, named in
`local.properties` (ignored by git):

```
signing.storeFile=androidApp/keystore.jks
signing.storePassword=…
signing.keyAlias=upload
signing.keyPassword=…
```

## Pull requests

- Keep each pull request to one change, with focused commits whose subject says what changed in the imperative
  ("Add a severity filter to logs").
- Run the checks listed under [Building and checks](#building-and-checks) and make sure they pass. They include
  the iOS compile: the Android tasks don't compile Kotlin/Native, so iOS-only breakage only shows up there.
- UI changes: update the screenshot references as described above and commit them, so reviewers see the visual
  change in the diff.
- Add or update tests for logic you change: mappers and repositories in `shared/data`, use cases in
  `shared/domain/usecase`, view models in `composeApp/src/commonTest`.
- A feature that calls a new PostHog endpoint needs its API key scope added to the login screen's scope list
  (`KeyScopes.kt`) and to the README.
- Add yourself to [AUTHORS](AUTHORS) in your first pull request.

## License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE), the
license of this project.
