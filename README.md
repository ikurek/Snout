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
- **Experiments**: status, variants, metrics and conclusion (read-only).
- **Annotations**: timeline of project annotations and a sheet to add one.
- **Activity log**: who changed what in the project, with before/after values.
- **Settings**: account, instance, selected project, light/dark/system theme, log out.
- Every screen has loading, empty, error, permission-denied and rate-limited states, pull to refresh, offline cache,
  and an "open in PostHog" action.

## Personal API key scopes

Read: `user`, `organization`, `project`, `dashboard`, `insight`, `query`, `feature_flag`, `experiment`,
`annotation`, `activity_log`. Add `feature_flag:write` to toggle flags and `annotation:write` to add annotations.
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
