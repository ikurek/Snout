# Snout

Pocket-sized PostHog. A Kotlin Multiplatform app (Android + iOS, Compose Multiplatform UI) that connects
straight to your PostHog instance with a personal API key. No backend of its own, nothing leaves your device
except calls to the instance you configure.

## What it does

- **Analytics**: web analytics overview (visitors, page views, sessions, duration, bounce rate) with
  period-over-period deltas, a visitors trend and top pages / sources / countries / devices for today, 7, 30 or 90 days.
- **Dashboards**: list of project dashboards, pinned first; open one to see every tile rendered natively
  (trends as line or bar, bold numbers, pie/donut, funnels, SQL tables). Tiles PostHog has not cached yet are computed on demand.
- **Insights**: saved insights with server-side search, favorites, and a detail view with the chart and metadata.
- **Feature flags**: search, status, rollout conditions and variants. Toggle a flag on or off with a confirmation;
  a key without the write scope gets a clear explanation instead of a failure.
- **Errors**: error tracking issues with search, an active / resolved / suppressed filter and an app version filter
  (versions that reported exceptions this week), each with its occurrences, users and a 7-day sparkline; open one
  for its impact chart, where it happened and the latest release, and resolve, suppress or reopen it with undo.
  Instances without the error tracking API get an "unavailable" state.
- **Logs**: services sending OpenTelemetry logs to PostHog with their volume, error rate and a sparkline for the
  last hour, day or week; open one for its latest lines with search, a level filter and older pages on scroll, and
  tap a line for its full message, trace ID and attributes.
- **Experiments**: status, variants, metrics and conclusion (read-only).
- **Annotations**: timeline of project annotations and a sheet to add one.
- **Activity log**: who changed what in the project, with before/after values.
- **Settings**: account, instance, selected project, light/dark/system theme, log out. Customise which four
  sections sit in the bottom bar (the rest live under More) and which opens first, and what the Analytics
  tab shows: default range, stat cards, visitors chart and breakdown tables. An open-source libraries screen lists
  every bundled library with its version, author and license, and opens each for its full license text and website.
- Every screen has loading, empty, error, permission-denied and rate-limited states, pull to refresh and an
  "open in PostHog" action.

## Personal API key scopes

Read: `user`, `organization`, `project`, `dashboard`, `insight`, `query`, `feature_flag`, `experiment`,
`annotation`, `activity_log`. Add `error_tracking:read` for Errors and `logs:read` for Logs. Add `feature_flag:write` to toggle flags,
`annotation:write` to add annotations, `error_tracking:write` to resolve or suppress errors and `insight:write`
to favorite insights. The login screen lists which scope each feature needs.
PostHog rate-limits personal keys (240 requests/min); the app surfaces this instead of failing silently.

## Project layout

```
androidApp/   Android entry point (SnoutApplication, MainActivity, launcher icons) and screenshot tests
iosApp/       iOS entry point (SwiftUI host), ShareExtension, SnoutLink (URL contract shared by both)
composeApp/   Shared Compose UI: design system, features (route / screen / view model), navigation
shared/       Kotlin business logic, one module per layer and feature:
  api/<feature>            Ktor API + DTOs
  domain/model/<feature>   domain types
  domain/data/<feature>    repository interfaces
  data/<feature>           repository implementations, mappers
  domain/usecase/<feature> use cases exposed to the UI
  storage/                 DataStore preferences, SQLDelight (organizations and projects) and the
                           secure store (KSafe) for the API key and account
  common/                  coroutine dispatchers, `@TestData` opt-in, test utilities
buildLogic/   Gradle convention plugins
scripts/      generate_screenshot_tests.py
```

## Architecture

Layers, with dependencies pointing inwards:

- `api` talks to PostHog and returns DTOs. `data` implements the `domain/data` repository interfaces: it calls
  `api` and maps DTOs to `domain/model` types. Repositories fetch (`getX(projectId, …): Either<DataError, T>`);
  mutations return the updated item where PostHog sends it back.
- API responses are not stored anywhere: a screen fetches what it shows when it needs it and holds it only in its
  view model's state. Only user data is persisted: organizations and projects in SQLDelight
  (`OrganizationRepository`), the API key and account in the secure store, and the instance URL, selected
  project and preferences in DataStore.
- Repositories with mutations expose `changes: Flow<Unit>` (a `ChangeSignal` in `data/common`), which emits after
  each successful mutation and carries no data. Lists reload through `Observe<Feature>Reloads` use cases
  (`ObserveSelectedProjectId.reloads(repository.changes)`): they emit a `ProjectReload` at once for the selected
  project, again when another project is selected, and again after each change, so a flag toggled on its detail
  screen shows in the list without the list knowing about it.
- DTOs sit in each `api` module's `model/` package, every field with a same-line `@SerialName`: responses are
  `XDto`, request bodies `XRequestDto` (`UpdateXRequestDto` for PATCHes), and what query endpoints take inside
  `{"query": …}` (`QueryRequestDto`) is `XQueryDto`, with its constant `kind` as an `@EncodeDefault` property.
  Optional request fields default to null and are left out, since `ApiJson` doesn't encode defaults.
- Secrets and personal data live in `SecureStore` (`storage/securestore`), never in DataStore. It is backed by
  [KSafe](https://github.com/ioannisa/KSafe): values encrypted with AES-256-GCM in KSafe's own `secure` file, under
  a key held in the Keychain on iOS (`AfterFirstUnlockThisDeviceOnly`, so it stays out of backups and device
  transfers) and in the Android Keystore on Android, with no decrypted copy cached in memory
  (`KSafeMemoryPolicy.ENCRYPTED`); a value that no longer decrypts reads as missing. The library is wrapped behind
  `SecureStore` so the rest of the app depends on a three-method interface it can fake (`InMemorySecureStore`),
  and swapping or upgrading the library stays inside this module. It holds the personal API key (`AuthDataStore`)
  and the signed-in account (`UserRepositoryImpl`, so Settings shows it at once). The instance URL, selected
  project and preferences stay in DataStore. Logging out removes the key and the account; a 401 removes the key and `ClearLocalData` the rest.
- HTTP responses are not cached by the platform: on iOS the Darwin engine's URLSession has no `URLCache`, and
  `startSnout()` empties the shared one; OkHttp on Android has no cache configured.
- `domain/usecase` depends only on repository interfaces. `composeApp` sees models and use cases (through
  `:shared`), never repositories or `data`; view models only call use cases. Many use cases are thin
  project-scoped wrappers (`repository.getX(getSelectedProjectId())`); that is intentional, so the UI never
  handles project ids.
- Repositories and use cases return Arrow `Either<DataError, T>`. Network failures become `DataError` via
  `NetworkError.toDataError()` (`data/common`). The UI words them with `designsystem/DataErrorMessages.kt` and
  shows full-screen failures with the placeholders in `designsystem/organisms/StatePlaceholders.kt`.

DI (Koin annotations with the Koin compiler plugin):

- Definitions are annotated on the classes themselves (`@Single`, `@Single(binds = [...])` for repository
  implementations, `@KoinViewModel`). Each module is a `@Module @ComponentScan` class, e.g.
  `FeatureFlagsDataModule`.
- `shared/src/commonMain/.../di/SharedModule.kt` is the shared composition root: it includes every data and use
  case module. `composeApp`'s `AppModule` includes it and scans the view models.
- `composeApp/.../Snout.kt` starts Koin once, before any UI: `SnoutApplication` on Android, `startSnout()` from
  `iOSApp.init` on iOS.
- Missing definitions fail the build through the compiler plugin's check of `SharedGraph`, and fail
  `AppModuleTest` for the app graph (view models included), which the plugin can't see.

Screens (`composeApp/.../features/<feature>/`, nested packages such as `detail/` for pushed screens):

- `XRoute.kt` wires the view model to the screen; `XScreen.kt` is stateless UI plus its previews;
  `XViewModel.kt`; `XUiState.kt` holds the state, the `XSideEffect` type if any, and `TestX…` fixtures for
  previews. `components/` holds the pieces, with labels in `XLabels.kt`
  (`val X.label: StringResource` and similar).
- One `uiState: StateFlow` per view model. Lists and other screens that reload use `RefreshableUiState`, built by
  `loadRefreshable`/`loadRefreshableList` (`common/state/RefreshableUiState.kt`) from the reload signal (plus any
  filter that changes what is loaded), the pull-to-refresh flow and the fetch: the first load shows skeletons,
  a reload keeps what was loaded on screen and shows a failure as a banner over it, a newer reload cancels the one
  running, and loading something else (another project, range or status) starts from the skeleton. One-shot
  fetches such as detail screens use `LoadState`. Live search and filter results (`searchResults`,
  `liveResults`) replace the list while active and fall back to it otherwise.
- Transient results (a failed favorite, navigation after an action) go through a `Channel` exposed as
  `sideEffects`. A failure is kept in the state only when it renders inline next to what failed (in a sheet or
  under a control).
- Handlers are named `on<Noun><PastParticiple>` (`onQueryChanged`, `onToggleConfirmed`); reloading is
  `refresh()`.
- Every destination is a `@Serializable` key in `navigation/NavKeys.kt` under `sealed interface SnoutNavKey`,
  which registers it for state saving automatically. Routes pass ids up through callbacks and
  `MainNavGraph` builds the keys.

Incoming links: Android `ACTION_VIEW`/`ACTION_SEND` intents (`MainActivity`) and iOS `onOpenURL` plus the share
extension (which reopens the app through a `snout://` URL defined in `iosApp/SnoutLink/SnoutLink.swift`) call
`submitIncomingLink`. The link stays pending until Home is in the back stack, then is resolved to a screen or
opened in the browser.

Tests are Kotest `ShouldSpec` with Mokkery mocks: mappers in `shared/data/*`, use cases in
`shared/domain/usecase/*`, view models in `composeApp/src/commonTest` (real use cases over mocked repositories),
request bodies in `shared/api/*` (pinned JSON against `FakePostHog`), the Koin graph in `AppModuleTest`, and
previews as screenshot tests (see Build). Shared fixtures sit next to the models behind the `@TestData` opt-in.

## Design system

The UI follows the "Ember" design: warm paper neutrals, a burnt-orange `primary` used only for actions
(filled buttons, switches, pins, links, the first chart series), tonal `secondaryContainer` for selection
(bottom nav, segmented control), and surface tiers instead of borders or shadows. Everything lives under
`composeApp/.../designsystem`:

- `tokens/` is the single source of colour, type, shape and spacing values (`SnoutTheme.colors`,
  `.extendedColors` for success and chart colours, `.typography`, `.code` for the monospace styles, `.shapes`,
  and the `Spacing` scale from `xxxSmall` 2dp to `huge` 48dp, with `large` 16dp for screen edges). Screens never
  hardcode a colour, `sp` or font family, and padding, gaps and spacers use `Spacing`; plain `dp` is left for
  component sizes (icons, heights, widths, radii, strokes) and a few component insets off the scale.
- `atoms/` are the smallest pieces: `StatusPill` and `Tag`, `TrendChip`, `SectionLabel`, `AvatarBox`,
  `BrandMark`, `Hairline` (the `outlineVariant` rule, optionally inset to the 16dp edges), skeleton blocks,
  buttons, chips and text fields.
- `molecules/` compose them: `EntityRow` (the only list row, with a `headline` slot overload for rich titles),
  `KeyValueRow`, `StatCard`, `SectionCard` and `TonalCard`, `CodeBlock` and `CodeChip`, `InfoBanner`,
  `InlineErrorBanner`, `SegmentedControl`, `Placeholder`.
- `organisms/` are screen-level: `PushedScreenScaffold` (back arrow, title, actions and open in PostHog for every
  screen pushed over the home shell), `SnoutTopAppBar`, `BottomNavigation`, `RefreshableStateContainer` and
  `LoadStateContainer` (skeleton loading, placeholders, refresh banner), `AnimatedBottomSheet` and
  `OptionPickerSheet`, `ConfirmationDialog`, and the state placeholders: `ErrorPlaceholder` (worded per
  `DataError`), `NothingFoundPlaceholder`, `NoSearchResultsPlaceholder` and `UnavailablePlaceholder` (a section
  the instance answers 404 for, via `RefreshableStateContainer`'s `notFoundContent`).
- `charts/` draw insight results with the `extendedColors.chart` palette.

Rules of thumb: lists are `EntityRow`s separated by `Hairline`s with no card per row; detail screens use
uppercase `SectionLabel`s over `KeyValueRow`s; status is always a word in a `StatusPill`, never a bare colour;
loading is skeleton rows, never a centred spinner (a small progress indicator inside a control that is
working, such as a button or the flag toggle, is fine); monospace is only for keys, values and event or
property names.

## Build

Android: `./gradlew :androidApp:assembleDebug`. iOS: open `iosApp` in Xcode (the framework is built by Gradle).
Checks before a pull request:

```
./gradlew testAndroidHostTest detekt :composeApp:compileKotlinIosSimulatorArm64
./gradlew :androidApp:validateDebugScreenshotTest
```

The Android tasks never compile Kotlin/Native, so the iOS compile is needed to catch iOS-only breakage. `detekt`
(with ktlint rules) only checks; `./gradlew detektFormat` fixes what the formatting rules can (it rewrites files,
so review the diff after it) and then `detekt` reports what is left.

Screenshot tests: every public `*Preview()` composable in `composeApp` is rendered in light and dark mode by
generated wrappers in `androidApp/src/screenshotTest` (Compose Preview Screenshot Testing). Wrappers are
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

## License

Snout is licensed under the [Apache License 2.0](LICENSE). Copyright 2026 [The Snout Authors](AUTHORS). See
[CONTRIBUTING.md](CONTRIBUTING.md) to get involved.
