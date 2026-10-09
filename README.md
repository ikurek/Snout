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
- **Experiments**: status, variants, metrics and conclusion (read-only).
- **Annotations**: timeline of project annotations and a sheet to add one.
- **Activity log**: who changed what in the project, with before/after values.
- **Settings**: account, instance, selected project, light/dark/system theme, log out. Customise which four
  sections sit in the bottom bar (the rest live under More) and which opens first, and what the Analytics
  tab shows: default range, stat cards, visitors chart and breakdown tables.
- Every screen has loading, empty, error, permission-denied and rate-limited states, pull to refresh, offline cache,
  and an "open in PostHog" action.

## Personal API key scopes

Read: `user`, `organization`, `project`, `dashboard`, `insight`, `query`, `feature_flag`, `experiment`,
`annotation`, `activity_log`. Add `error_tracking:read` for Errors. Add `feature_flag:write` to toggle flags,
`annotation:write` to add annotations and `error_tracking:write` to resolve or suppress errors.
PostHog rate-limits personal keys (240 requests/min); the app surfaces this instead of failing silently.

## Project layout

```
androidApp/   Android entry point (Activity, launcher icons)
iosApp/       iOS entry point (SwiftUI host, assets)
composeApp/   Shared Compose UI: design system, features (route / screen / view model), navigation
shared/       Kotlin business logic, one module per layer and feature:
  api/<feature>            Ktor API + DTOs
  domain/model/<feature>   domain types (serializable so they can be cached)
  domain/data/<feature>    repository interfaces
  data/<feature>           repository implementations, mappers, cache
  domain/usecase/<feature> use cases exposed to the UI
  storage/                 DataStore preferences and SQLDelight (organizations, JSON payload cache)
buildLogic/   Gradle convention plugins
```

Conventions: Koin annotations for DI, Arrow `Either` for errors, SQLDelight cache-first lists
(`CachedUiState` + `RefreshController`), Kotest + Mokkery unit tests, detekt with ktlint rules.

## Design system

The UI follows the "Ember" design: warm paper neutrals, a burnt-orange `primary` used only for actions
(filled buttons, switches, pins, links, the first chart series), tonal `secondaryContainer` for selection
(bottom nav, segmented control), and surface tiers instead of borders or shadows. Everything lives under
`composeApp/.../designsystem`:

- `tokens/` is the single source of colour, type and shape values (`SnoutTheme.colors`, `.extendedColors`
  for success and chart colours, `.typography`, `.code` for the monospace styles, `.shapes`). Screens never
  hardcode a colour, `sp` or font family.
- `atoms/` are the smallest pieces: `StatusPill` and `Tag`, `TrendChip`, `SectionLabel`, `AvatarBox`,
  `BrandMark`, skeleton blocks, buttons, chips and text fields.
- `molecules/` compose them: `EntityRow` (the only list row), `KeyValueRow`, `StatCard`, `SectionCard` and
  `TonalCard`, `CodeBlock` and `CodeChip`, `InfoBanner`, `InlineErrorBanner`, `SegmentedControl`,
  `Placeholder`.
- `organisms/` are screen-level: `CachedStateContainer` (skeleton loading, placeholders, refresh banner),
  `SnoutTopAppBar`, `BottomNavigation`, `ConfirmationDialog`, the error placeholders.
- `charts/` draw insight results with the `extendedColors.chart` palette.

Rules of thumb: lists are `EntityRow`s separated by `outlineVariant` hairlines with no card per row; detail
screens use uppercase `SectionLabel`s over `KeyValueRow`s; status is always a word in a `StatusPill`, never a
bare colour; loading is skeleton rows, never a centred spinner; monospace is only for keys, values and
event or property names.

## Build

Android: `./gradlew :androidApp:assembleDebug`. iOS: open `iosApp` in Xcode (the framework is built by Gradle).
Checks: `./gradlew testAndroidHostTest detekt`.

Screenshot tests: every public `*Preview()` composable in `composeApp` is rendered in light and dark mode by
generated wrappers in `androidApp/src/screenshotTest` (Compose Preview Screenshot Testing). Wrappers are
grouped per preview package, so the reference images under `androidApp/src/screenshotTestDebug/reference`
mirror the package tree. Verify with `./gradlew :androidApp:validateDebugScreenshotTest`. After adding or
renaming previews run `scripts/generate_screenshot_tests.py`; after an intended UI change regenerate the
references with `./gradlew :androidApp:updateDebugScreenshotTest` and commit them.

If Gradle cannot find the Android SDK, add a `local.properties` with `sdk.dir=/path/to/Android/sdk`.
