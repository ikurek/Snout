# Snout architecture

How the code is organised and the conventions new code follows. Start with [CONTRIBUTING.md](../CONTRIBUTING.md)
for how to build, check and send a change.

## Project layout

```
androidApp/   Android entry point (SnoutApplication, MainActivity, launcher icons), the Glance widgets
              (appwidget/) and screenshot tests
iosApp/       iOS entry point (SwiftUI host), ShareExtension, SnoutWidgets (WidgetKit extension), SnoutLink (URL
              contract shared by all three), KotlinMultiplatformLinkedPackage (posthog-ios for the telemetry SDK,
              generated; see Telemetry)
composeApp/   Shared Compose UI: design system, features (route / screen / view model), navigation
widgets/      What the home-screen widgets show (loaders and their states), without Compose; on iOS the
              SnoutWidgetKit framework the widget extension links (see Widgets)
shared/       Kotlin business logic, one module per layer and feature:
  api/<feature>            Ktor API + DTOs
  domain/model/<feature>   domain types
  domain/data/<feature>    repository interfaces
  data/<feature>           repository implementations, mappers
  domain/usecase/<feature> use cases exposed to the UI
  storage/                 DataStore preferences, SQLDelight (organizations and projects) and the
                           secure store (KSafe) for the account, and the API key's store
  common/                  `@TestData` opt-in, test utilities, number formatting (`format`) the screens and
                           widgets share
  telemetry/               Snout's own usage and error reports: ErrorTracker, EventSink, TelemetrySwitch,
                           TrackNonFatal and fakes; free of SDKs, so any layer can report
  telemetry/sdk            PostHog (posthog-kmp) behind them; only `shared` sees it
  core/                    CoreModule: every data and use case module, without the telemetry SDK
  widgets/                 WidgetSharedModule: the iOS widget extension's composition root (CoreModule, no
                           reporting)
buildLogic/   Gradle convention plugins
konsist/      Konsist architecture tests (JVM; reads the other modules' sources)
scripts/      generate_screenshot_tests.py
```

## Architecture

Layers, with dependencies pointing inwards:

- `api` talks to PostHog and returns DTOs. `data` implements the `domain/data` repository interfaces: it calls
  `api` and maps DTOs to `domain/model` types. Repositories fetch (`getX(projectId, …): Either<DataError, T>`);
  mutations return the updated item where PostHog sends it back.
- API responses are not stored anywhere: a screen fetches what it shows when it needs it and holds it only in its
  view model's state. Only user data is persisted: organizations and projects in SQLDelight
  (`OrganizationRepository`), the API key and account in secure storage, and the instance URL, selected
  project and preferences in DataStore. Widgets follow the same rule: each refresh loads what the widget shows
  again, and a refresh that fails shows its error rather than anything loaded before (see Widgets).
- Repositories with mutations expose `changes: Flow<Unit>` (a `ChangeSignal` in `data/common`), which emits after
  each successful mutation and carries no data. Lists reload through `ObserveProjectReloads`, or for a feature with
  mutations its `Observe<Feature>Reloads` (`observeProjectReloads(repository.changes)`): they emit a `ProjectReload`
  at once for the selected project and again when another project is selected (`projectChanged = true`), and again
  after each change (`projectChanged = false`), so a flag toggled on its detail screen shows in the list without the
  list knowing about it.
- DTOs sit in each `api` feature module's `model/` package (the ones several features share, such as
  `PaginatedResponseDto` and `QueryRequestDto`, in `shared/api` itself), every field with a same-line `@SerialName`: responses are
  `XDto`, request bodies `XRequestDto` (`UpdateXRequestDto` for PATCHes), and what query endpoints take inside
  `{"query": …}` (`QueryRequestDto`) is `XQueryDto`, with its constant `kind` as an `@EncodeDefault` property.
  Optional request fields default to null and are left out, since `ApiJson` doesn't encode defaults.
- HogQL queries (`HogQLQueryDto` through `query/`) pass anything that comes from outside the query, such as an issue
  or event id, as `values` that fill its `{placeholders}`, never pasted into the query text. Queries on `events`
  always bound `timestamp`, since the table is sorted by time.
- Secrets and personal data live in `SecureStore` (`storage/securestore`), never in DataStore. It is backed by
  [KSafe](https://github.com/ioannisa/KSafe): values encrypted with AES-256-GCM in KSafe's own `secure` file, under
  a key held in the Keychain on iOS (`AfterFirstUnlockThisDeviceOnly`, so it stays out of backups and device
  transfers) and in the Android Keystore on Android, with no decrypted copy cached in memory
  (`KSafeMemoryPolicy.ENCRYPTED`); a value that no longer decrypts reads as missing. The library is wrapped behind
  `SecureStore` so the rest of the app depends on a three-method interface it can fake (`InMemorySecureStore`),
  and swapping or upgrading the library stays inside this module. It holds the signed-in account
  (`UserRepositoryImpl`, so Settings shows it at once). The instance URL, selected project and preferences stay in
  DataStore.
- The personal API key has its own store, `PersonalApiKeyStore` (faked by `InMemoryPersonalApiKeyStore`), which
  `AuthDataStore` uses. It is separate because the iOS widget extension needs the key too, and KSafe can't be
  shared between processes: it reads its file once and serves it from memory, and writes the file when it starts,
  so a second process would see stale values and could overwrite the app's. Each platform keeps the key where its
  widgets can reach it:
  - Android: in KSafe with everything else (`SecureStorePersonalApiKeyStore`). Glance widgets run in the app's
    process and graph, so nothing has to be shared. Moving the key to hand-written Keystore code would add our own
    encryption and a migration for every user, for no gain.
  - iOS: one Keychain item in an access group the app and the widget extension share (`SharedKeychain`), so both
    use the same key, and signing out or in reaches widgets on their next load. The app's
    `KeychainPersonalApiKeyStore` reads and writes it; the extension's `WidgetPersonalApiKeyStore` reads it fresh
    each time and never writes it. Versions before widgets kept the key in KSafe: the first read moves it to the
    Keychain. iOS keeps Keychain items after the app is deleted, but deletes the preferences with the instance URL:
    `AuthDataStore` drops a key it finds without its instance, so a reinstall starts signed out (`PRIVACY.md` says
    so); signing in saves the instance first. Both targets must have the access group (`SharedWithWidgets`).
- Logging out removes the key, the account, the organizations and projects
  and the selected project (`Logout`, through `ClearLocalData`); a 401 removes the key, and `ClearLocalData` the rest.
- HTTP responses are not cached by the platform: on iOS the Darwin engine's URLSession has no `URLCache`, and
  `startSnout()` empties the shared one; OkHttp on Android has no cache configured.
- PostHog's answers are decoded off the main thread: `networkCall` runs the call on `Dispatchers.Default`, since Ktor
  decodes the body in the calling coroutine and view models call from `viewModelScope`, on the main thread.
- Debug builds log HTTP traffic (Ktor `Logging`, to logcat and the iOS unified log; `shared/api/DebugLogging.kt`):
  request lines, statuses and headers, with `Authorization` masked and bodies left out, plus the cause of each failure
  `networkCall` reports as `UnexpectedError` (an exception, or a status and request line). Release builds log nothing;
  `PlatformApiModule` tells them apart (Android's `FLAG_DEBUGGABLE`, Kotlin/Native's `Platform.isDebugBinary`).
- `domain/usecase` depends only on repository interfaces. `composeApp` sees models and use cases (through
  `:shared`), never repositories or `data`; view models only call use cases. Many use cases are thin
  project-scoped wrappers (`repository.getX(getSelectedProjectId())`); that is intentional, so the UI never
  handles project ids.
- Repositories and use cases return Arrow `Either<DataError, T>`, as `api` calls already do: `networkCall`
  (`shared/api`) maps a failed call to its `DataError`. The UI words them with `designsystem/DataErrorMessages.kt`
  and shows full-screen failures with the placeholders in `designsystem/organisms/StatePlaceholders.kt`.

DI (Koin annotations with the Koin compiler plugin):

- Definitions are annotated on the classes themselves (`@Single`, `@Single(binds = [...])` for repository
  implementations, `@KoinViewModel`). Each module is a `@Module @ComponentScan` class, e.g.
  `FeatureFlagsDataModule`.
- `shared/src/commonMain/.../di/SharedModule.kt` is the shared composition root: `CoreModule` (`shared/core`, every
  data and use case module) plus the telemetry SDK. `composeApp`'s `AppModule` includes it and the widget loaders
  (`WidgetsModule`), and scans the view models. The iOS widget extension builds its own graph on `CoreModule` (see
  Widgets).
- `composeApp/.../Snout.kt` starts Koin once, before any UI, and then applies the telemetry settings (see
  Telemetry): `SnoutApplication` on Android, `startSnout()` from `iOSApp.init` on iOS.
- Before any UI the platform also reads `Startup` with `loadStartup()` (`composeApp/.../Startup.kt`): the first
  screen, the theme and Home's bar, local reads only. `App(startup)` opens straight on them, so the first frame is
  already the right screen in the right theme, and keeps following theme and bar changes afterwards. Android keeps
  its launch screen (`core-splashscreen`, `Theme.Snout.Starting`) up while `MainActivity` reads it in a view model
  that survives rotation; iOS can't extend its launch screen, so `MainViewController` reads it with `runBlocking`
  while the launch screen still shows.
- Missing definitions fail the build through the compiler plugin's check of `SharedGraph`, and fail
  `AppModuleTest` for the app graph (view models included), which the plugin can't see.

Screens (`composeApp/.../features/<feature>/`, nested packages such as `detail/` for pushed screens):

- `XRoute.kt` wires the view model to the screen; `XScreen.kt` is stateless UI plus its previews;
  `XViewModel.kt`; `XUiState.kt` holds the state, the `XSideEffect` type if any, and `TestX…` fixtures for
  previews. `components/` holds the pieces, with labels in `XLabels.kt`
  (`val X.label: StringResource` and similar).
- One `uiState: StateFlow` per view model. View models that load from PostHog back it with a
  `MutableStateFlow<XUiState>` they change with `update {}`; those that only show local data (Settings and its
  sub-screens, Home, the project switcher and picker) build it with `stateIn` from what they observe, and the
  settings screens that show a stored preference as it is expose that domain type. Each loading screen's state is its own data class with fields named for its data: the loaded data, null until the first
  load succeeds (`flags: List<FeatureFlag>?`), `error: DataError?` and `isRefreshing`, next to the screen's filters
  and statuses. Nothing loaded and no error is the skeleton; an error with nothing loaded is the error placeholder;
  an error with data loaded is a banner over it. There are no shared state types or loading helpers: each view
  model has a private `load()` that cancels the previous load's `Job`, keeps what is loaded on screen marked as
  refreshing, calls the use case and folds the `Either` into the state. Lists collect their reloads and call `load()`
  on each `ProjectReload`; another project (`projectChanged`, or range, or status) clears the data first, so it
  starts from the skeleton. Live search and filter results (feature flags, insights, error tracking) sit in their
  own fields (`searchResults`, `filteredIssues`), fetched by the view model once typing pauses (`debounce` with
  `collectLatest`), and replace the list on screen while a query or filter is set.
- Every screen showing fetched data refreshes each time it is entered: its Route calls
  `RefreshOnEnter(viewModel::onScreenEntered)` (`common/compose`, a `LifecycleResumeEffect`), which runs on every
  resume: navigating to the screen or back to its tab, popping the screen pushed over it (Navigation 3 resumes an
  entry once its transition settles), and returning from the background (iOS too). `onScreenEntered` refreshes as
  pull to refresh does except on the first call (the first load is already running then) and while a load runs, so
  loads never stack. Settings refreshes the account the same way; onboarding and bundled data (libraries) don't.
- Transient results (a failed favorite, navigation after an action) go through a `Channel` exposed as
  `sideEffects`. A failure is kept in the state only when it renders inline next to what failed (in a sheet or
  under a control).
- Handlers of what the person did are named `on<Noun><PastParticiple>` (`onQueryChanged`, `onToggleConfirmed`);
  reloading is `refresh()`. Some older actions are plain verbs (`toggleFavorite`, `loadMore`, `retryTile`, `reset`);
  new handlers follow the `on…` form.
- Every destination is a `@Serializable` key in `navigation/NavKeys.kt` under `sealed interface SnoutNavKey`,
  which registers it for state saving automatically. Routes pass ids up through callbacks and
  `MainNavGraph` builds the keys.

Incoming links: Android `ACTION_VIEW`/`ACTION_SEND` intents (`MainActivity`) and iOS `onOpenURL` plus the share
extension (which reopens the app through a `snout://` URL defined in `iosApp/SnoutLink/SnoutLink.swift`) call
`submitIncomingLink`. The link stays pending until Home is in the back stack, then is resolved to a screen or
opened in the browser.

Tests are Kotest `ShouldSpec` with Mokkery mocks: mappers in `shared/data/*`, use cases in
`shared/domain/usecase/*` (one test file per use case, named after it), view models in `composeApp/src/commonTest` (real use cases over mocked repositories),
request bodies in `shared/api/*` (pinned JSON against `FakePostHog`), the Koin graph in `AppModuleTest`, and
previews as screenshot tests (see [CONTRIBUTING.md](../CONTRIBUTING.md#building-and-checks)). Shared fixtures sit next to the models behind the `@TestData` opt-in.
The layering and naming conventions on this page are enforced by the Konsist tests in `konsist/`.

## Telemetry

Snout reports how it is used and what breaks to its own PostHog project. This is the only place the app sends
anything other than calls to the person's instance, so it follows a few rules:

- **Opt-out, per kind.** Usage (screen views, `TelemetryEvent`s, the app's opens and closes) and error reports
  (non-fatal errors and crashes) are each on until switched off in Settings → Privacy (`TelemetrySettings`, stored in
  `TelemetryDataStore`; logging out keeps them). `startSnout()` applies the stored choice before any UI
  (`ApplyTelemetrySettings`), and saving applies it at once. In builds that never report, the Privacy screen says so
  instead of offering switches (`IsTelemetryAvailable`, from `TelemetrySwitch.isAvailable`).
- **Only store builds report.** `telemetry/sdk` starts PostHog only in release builds that have a project token
  (`PostHogSettings`, from BuildKonfig: Codemagic's `posthog` group, or `posthog.project.token` and `posthog.host` in
  `local.properties`). Debug builds and builds without a token never start it, and every call is then a no-op.
- **Nothing from the person's PostHog.** `TelemetryEvent`s carry kinds, counts and switches only (Konsist fails a
  `String`, `Long` or `Uuid` in one); screens are a `TelemetryScreen` kind, never a nav key with its ids; the instance is cloud US, cloud EU or self-hosted (`InstanceKind`). Error reports lose their messages in the
  SDK's `beforeSend` (`filteredProperties`), and `networkCall`'s reports name the endpoint as a redacted path
  (`/api/projects/:id/insights/:id/`). PostHog runs with no person profiles and no identify.
- **One way in.** As in the rest of the app, the domain has a `telemetry` feature: `TelemetryEvent` and
  `TelemetryScreen` (model), `TelemetryRepository` (interface), the data module that maps events to snake_case wire
  names and properties for `EventSink`, and a use case per event (`TrackFeatureFlagToggled`, `TrackScreenViewed`…).
  View models call those use cases, after PostHog accepted a change or on a screen's first load, not on refreshes;
  a picked project says where it was picked (`ProjectSelectionSource`). Screen views are navigation's: `MainNavGraph`
  reports the destination on top of the back stack (`SnoutNavKey.telemetryScreen`, exhaustive, so a new destination
  must say how it reports), and Home and onboarding, whose tabs and steps aren't destinations, report the shown tab
  and the settled step from their Routes. Unexpected failures in `networkCall` reach
  `TrackNonFatal` through `FailureReport`, set when the client is built. Only `telemetry/sdk` may import PostHog's
  SDK, and the UI never sees `shared/telemetry` (Konsist).

On iOS, `posthog-kmp` brings `posthog-ios` through Kotlin's SwiftPM import: the generated
`iosApp/KotlinMultiplatformLinkedPackage` is committed and linked by the app target. After changing `posthog-kmp`'s
version, regenerate it with `XCODEPROJ_PATH=iosApp/iosApp.xcodeproj ./gradlew :composeApp:integrateLinkagePackage`,
move the package from `composeApp/iosApp/` to `iosApp/`, and restore the project file: the task rewrites it, while the
four entries the package needs are already there.

## Widgets

Home-screen widgets show one analytics stat over a range (wide widgets add the other stats) and one saved insight
(its latest number and line, its top breakdowns, or its funnel conversion), and a project's highlights: up to nine
metrics picked per widget from five sources, each queried only when a metric needs it (`LoadProjectHighlights`):
PostHog's web overview, active users (one trends query; daily users are the last whole day's, since today's isn't over,
and stickiness is that day's daily over monthly users), revenue analytics (MRR, revenue to date, subscribers and paying
customers from `persons_revenue_analytics`, which totals every revenue source, in the project's `base_currency`; "Not
set up" without any customers) and app health (the newest `$app_version` of the last week by its numbers, and
`$exception` events over the range with the users they hit, both HogQL). PostHog counts MRR only from subscriptions:
revenue events add to it only once their revenue analytics config names a subscription property, so a project whose
customers paid but none of it recurs shows "No subscriptions" for MRR and subscribers rather than zero. A source that
fails marks its tiles, not the widget, which shows the error only when they all did. The analytics widget is called
"Analytics", like the tab, not "Web analytics": PostHog's web analytics queries count `$screen` as well as `$pageview`
(`shared/api/analytics/Queries.kt`), so it works for mobile apps too.

Each widget has its own project, picked in its settings and independent of the app's selected project. A new widget
starts on none, so the person picks it rather than inheriting whatever the app shows. Both platforms ask the same
things in the same order (analytics: project, stat, range; insight: project, then insight; highlights: project, range,
metrics) with the same defaults (Visitors, last 7 days; daily and monthly users, session duration and exceptions). The
highlights widget draws a grid of cards (name, value, change), as many as fit in the fewest columns that show them;
on Android `highlightsLayout` works the grid out from the widget's size and font scale, measuring text with the font
padding RemoteViews keep so no row is cut off, and drops the change to an arrow by the name before it would. Its
header says how many metrics didn't fit ("Last 7 days · +2"). Its metrics keep the order picked (Android's settings
list the picks to reorder; iOS's multi-value parameter is ordered and capped per size: four small, six medium, nine
large). Every widget heads itself with its project's name, kept with its settings as it was when
picked (Android's Glance state, iOS's `ProjectEntity`), so widgets of different projects can be told apart. Both
lay a stat or insight out the same way: the header (title, caption and a refresh button; on iOS `RefreshWidgetIntent`,
which WidgetKit answers by reloading that widget), the number and its change (on wide widgets with the next stats
beside it), and its line as a chart filling the rest of the widget edge to edge (ending on a dot at the right edge), with
when it last loaded on the
chart's faded fill (`WidgetChart` and `sparkline` on Android, `WidgetChartLayout` and `Sparkline` on iOS, which turns
the system's content margins off for it). Without a line, the other stats and the footer take its place, as the
height allows.

Compose Multiplatform can't draw widgets on either platform, so the UI is native and everything below it is shared:

- `widgets/` holds the loaders (`AnalyticsWidgetLoader`, `InsightWidgetLoader`, `HighlightsWidgetLoader`, and `WidgetProjects` for the
  settings' project list): they call the use cases, as a view model would, naming the widget's project (the
  project-scoped use cases take an optional `projectId`, the selected project's otherwise), and return a
  `WidgetState` (signed out per `IsSignedIn`, failed with a `DataError`, or loaded) with the values already formatted (`shared/common/format`, the same
  functions the screens use). Tapping a loaded widget opens its PostHog link (`GetPostHogUrl`), which the app
  resolves to its own screen, switching to the widget's project. The insight picker on both platforms offers only insights a widget can show
  (`InsightSummary.fitsWidget`).
- Nothing is cached for widgets: every refresh loads again, and a failed refresh shows its error (a tap tries again
  when that can help, or opens the app). The platforms refresh every 30 minutes as far as their budgets allow, on
  the widget's refresh button, and at once when the key changes (signing in or out): `observeWidgetChanges` (`Snout.kt`,
  on `ObserveWidgetChanges`) reloads them, through `reloadAll` on Android (`startWidgets`, which also hands the
  picker its previews) and WidgetKit's `reloadAllTimelines` on iOS. Without it, a widget would go on showing the
  account that signed out, or asking to sign in, until its next refresh.
- Android: Glance widgets in `androidApp/.../appwidget`, in the app's process and graph, drawn for their exact size
  (`SizeMode.Exact`; the sparkline bitmap is drawn at the size it is shown). Each widget kind has a `WidgetResults`
  (`AnalyticsResults`, `InsightResults`, in `WidgetReloads.kt`) holding how it reads its settings and the `widgets`
  loader it loads with. `provide` loads before the session draws, which Glance doesn't time, so the launcher keeps
  the previous drawing (or Glance's loading layout for a new widget) instead of an empty frame; `reload` (refresh
  button, saved settings, sign-in or out) shows a spinner on what is drawn, loads in `WidgetScope`, outside the
  session, then redraws. Glance cancels a session 45 seconds after its first drawing (a tap adds 5) and an insight
  can take PostHog up to 90, so a load running in it could be cut short with the spinner still drawn; nor can a
  session be closed to load again before its first drawing. That is all the store is for: it holds each widget's
  last load in memory only, like a view model's state, and drops it when the widget is removed. A second refresh
  cancels the first. Each widget has a configuration activity (`WidgetConfigurationActivity`)
  showing a Compose screen from `composeApp/.../features/widgets` (`AnalyticsWidgetSettings`,
  `InsightWidgetPicker`), each setting a dropdown that opens a menu under it (`WidgetDropdownRow`,
  `WidgetProjectRow`); the picked project, stat and range, or the project and the insight's id and name, are the
  widgets' only Glance state. A widget placed before it had a project shows "tap to
  set up" until it gets one. Home screen only (`not_keyguard`). Colours copy the Ember tokens (`WidgetColors`, and
  `widget_colors.xml` for the frame before Android 12), since Glance can't use the Compose theme. There is no XML
  layout: the widget picker's previews are the widgets' own UI with sample numbers (`providePreview`,
  `WidgetPreviews.kt`), handed to Android at start (Android 15 and later; earlier pickers show the app's icon). The
  refresh icon is a vector drawable, since RemoteViews take images by resource, not as `ImageVector`s.
- iOS: the `SnoutWidgets` extension is SwiftUI, with App Intents to pick the project (`ProjectEntity`) and then the
  stat and range or the insight (whose list depends on the picked project). Its text is in `Localizable.xcstrings`,
  under keys named as Android's `widget_*` strings, read through `Strings`; App Intents read their titles at build
  time, so they name their keys in place, and their summaries use the English text as the key. It links `SnoutWidgetKit`
  (`:widgets`' framework; no Compose, no PostHog SDK) and runs its own graph (`SnoutWidgets.start`:
  `WidgetSharedModule` and `WidgetsModule`). It reads what the app shares and never writes it (the project list it fetches for the settings goes into its own
  database, not the app's), and reads it again before every load, since WidgetKit keeps the extension's process
  for several refreshes while the app may sign out or in:
  - The DataStore preferences live in the App Group container (`group.com.nightcode.snout`; the app moves them
    there from Documents once). The extension doesn't open a DataStore on them (one reads its file once and serves it
    from memory): `AppPreferencesReader` reads the file on every collection.
  - The personal API key is the shared Keychain item (see `PersonalApiKeyStore` above), read again before every load
    (`WidgetSession` makes `AuthDataStore` drop the key it holds in memory). Nothing else in the secure store is
    shared: the extension's `SecureStore` (`WidgetSecureStore`) is empty, and it never opens the app's KSafe file.
  - The groups are in both targets' entitlements, and each target's Info.plist resolves the Keychain group's team
    prefix (`SnoutKeychainAccessGroup`). Values are `privacySensitive`, so they hide on a locked device when the person
  doesn't allow widgets there.

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
  screen pushed over the home shell; the feature flag detail builds the same bar itself for its monospace title), `SnoutTopAppBar`, `BottomNavigation`, the pieces a screen picks from its own
  state (`RefreshableContent.kt`: `LoadingOrErrorContent` for the skeleton or the error placeholder,
  `RefreshableContent` for loaded data under pull to refresh and the refresh-failed banner,
  `RefreshableEmptyContent`), `AnimatedBottomSheet` and `OptionPickerSheet`, `ConfirmationDialog`, and the state
  placeholders: `ErrorPlaceholder` (worded per `DataError`), `NothingFoundPlaceholder`,
  `NoSearchResultsPlaceholder` and `UnavailablePlaceholder` (a section the instance answers 404 for, via
  `LoadingOrErrorContent`'s `notFoundContent`).
- `charts/` draw insight results with the `extendedColors.chart` palette.

Rules of thumb: lists are `EntityRow`s separated by `Hairline`s with no card per row; detail screens use
uppercase `SectionLabel`s over `KeyValueRow`s; status is always a word in a `StatusPill`, never a bare colour;
loading is skeleton rows, never a centred spinner (a small progress indicator inside a control that is
working, such as a button or the flag toggle, is fine); monospace is only for keys, values and event or
property names.
