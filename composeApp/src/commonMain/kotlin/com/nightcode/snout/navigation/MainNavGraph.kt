package com.nightcode.snout.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.nightcode.snout.features.dashboards.detail.DashboardDetailRoute
import com.nightcode.snout.features.errortracking.detail.ErrorTrackingIssueRoute
import com.nightcode.snout.features.experiments.detail.ExperimentDetailRoute
import com.nightcode.snout.features.featureflags.detail.FeatureFlagDetailRoute
import com.nightcode.snout.features.home.HomeRoute
import com.nightcode.snout.features.home.HomeSectionRoute
import com.nightcode.snout.features.home.SectionNavigation
import com.nightcode.snout.features.insights.detail.InsightDetailRoute
import com.nightcode.snout.features.logs.entry.LogEntryRoute
import com.nightcode.snout.features.logs.service.LogServiceRoute
import com.nightcode.snout.features.onboarding.OnboardingRoute
import com.nightcode.snout.features.settings.SettingsRoute
import com.nightcode.snout.features.settings.analytics.AnalyticsSettingsRoute
import com.nightcode.snout.features.settings.libraries.LibrariesRoute
import com.nightcode.snout.features.settings.libraries.detail.LibraryDetailRoute
import com.nightcode.snout.features.settings.navigation.NavigationSettingsRoute
import com.nightcode.snout.features.settings.privacy.PrivacySettingsRoute
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.links.LinkResolution
import com.nightcode.snout.shared.domain.usecase.appstate.DetermineInitialLocation
import com.nightcode.snout.shared.domain.usecase.auth.ClearLocalData
import com.nightcode.snout.shared.domain.usecase.auth.ShouldReturnToLogin
import com.nightcode.snout.shared.domain.usecase.links.ClearPendingLink
import com.nightcode.snout.shared.domain.usecase.links.ObservePendingLink
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import com.nightcode.snout.shared.domain.usecase.links.ResolvePostHogLink
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackLinkOpened
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackScreenViewed
import kotlinx.coroutines.flow.filterNotNull
import org.koin.compose.koinInject

private val PushTransition: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
}

private val PopTransition: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
}

/** [initialNavigationPreferences] seeds Home, so it draws its bar on its first frame. */
@Composable
internal fun MainNavGraph(initial: SnoutNavKey, initialNavigationPreferences: NavigationPreferences) {
    val backStack = rememberNavBackStack(DefaultSavedStateConfiguration, initial)
    ReturnToLoginOnSessionExpiry(backStack)
    HandleIncomingLinks(backStack)
    ReportScreenViews(backStack)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = PushTransition,
        popTransitionSpec = PopTransition,
        predictivePopTransitionSpec = { PopTransition() },
        entryProvider = mainEntryProvider(backStack, initialNavigationPreferences),
    )
}

/**
 * Wipes local data and restarts onboarding once the API key disappears: at the intro after a logout, which also
 * forgets the instance, or at the login step after a 401, which keeps it.
 */
@Composable
private fun ReturnToLoginOnSessionExpiry(backStack: NavBackStack<NavKey>) {
    val shouldReturnToLogin = koinInject<ShouldReturnToLogin>()
    val clearLocalData = koinInject<ClearLocalData>()
    val determineInitialLocation = koinInject<DetermineInitialLocation>()

    LaunchedEffect(Unit) {
        shouldReturnToLogin().collect {
            if (backStack.none { it is OnboardingNavKey }) {
                clearLocalData()
                val onboarding = determineInitialLocation().toNavKey()
                backStack.clear()
                backStack.add(onboarding)
            }
        }
    }
}

/**
 * Opens the pending link once Home is in the back stack; until then (cold start, login, project selection) it
 * stays pending. The stack is cut back to Home first, so Back from the linked screen lands there.
 */
@Composable
private fun HandleIncomingLinks(backStack: NavBackStack<NavKey>) {
    val observePendingLink = koinInject<ObservePendingLink>()
    val clearPendingLink = koinInject<ClearPendingLink>()
    val resolvePostHogLink = koinInject<ResolvePostHogLink>()
    val openInBrowser = koinInject<OpenInBrowser>()
    val trackLinkOpened = koinInject<TrackLinkOpened>()
    val hasHome = backStack.contains(HomeNavKey)

    LaunchedEffect(hasHome) {
        if (!hasHome) return@LaunchedEffect
        observePendingLink().filterNotNull().collect { url ->
            clearPendingLink()
            val resolution = resolvePostHogLink(url)
            trackLinkOpened(resolution)
            when (resolution) {
                is LinkResolution.InApp -> {
                    backStack.popUpTo(HomeNavKey)
                    resolution.destination.toNavKey()?.let(backStack::add)
                }

                is LinkResolution.Browser -> openInBrowser(resolution.url)
            }
        }
    }
}

/**
 * Reports the destination on top of the back stack as viewed, each time another one comes on top (pushed, or
 * uncovered by a pop). Home and onboarding report their own tabs and steps, which aren't destinations.
 */
@Composable
private fun ReportScreenViews(backStack: NavBackStack<NavKey>) {
    val trackScreenViewed = koinInject<TrackScreenViewed>()
    val top = backStack.lastOrNull() as? SnoutNavKey
    LaunchedEffect(top) {
        top?.telemetryScreen?.let { trackScreenViewed(it) }
    }
}

/** Removes every entry above the last [key], which stays on top; nothing when [key] isn't in the stack. */
private fun NavBackStack<NavKey>.popUpTo(key: SnoutNavKey) {
    val index = lastIndexOf(key)
    if (index < 0) return
    while (lastIndex > index) removeAt(lastIndex)
}

/* One group of entries per area of the app, each with the navigation its screens need. */

private fun mainEntryProvider(
    backStack: NavBackStack<NavKey>,
    initialNavigationPreferences: NavigationPreferences,
): (NavKey) -> NavEntry<NavKey> {
    val back: () -> Unit = { backStack.removeLastOrNull() }
    return entryProvider {
        onboardingEntries(backStack)
        homeEntries(backStack, back, initialNavigationPreferences)
        settingsEntries(backStack, back)
        detailEntries(backStack, back)
        logsEntries(backStack, back)
    }
}

private fun EntryProviderScope<NavKey>.onboardingEntries(backStack: NavBackStack<NavKey>) {
    entry<OnboardingNavKey> { key ->
        OnboardingRoute(
            navKey = key,
            onFinished = {
                backStack.clear()
                backStack.add(HomeNavKey)
            },
        )
    }
}

/** Home with its tabs, and a section opened from More; both open the same detail screens. */
private fun EntryProviderScope<NavKey>.homeEntries(
    backStack: NavBackStack<NavKey>,
    back: () -> Unit,
    initialNavigationPreferences: NavigationPreferences,
) {
    val sectionNavigation = SectionNavigation(
        onOpenDashboard = { backStack.add(DashboardDetailNavKey(it)) },
        onOpenInsight = { backStack.add(InsightDetailNavKey(it)) },
        onOpenFeatureFlag = { backStack.add(FeatureFlagDetailNavKey(it)) },
        onOpenErrorTrackingIssue = { backStack.add(ErrorTrackingIssueNavKey(it)) },
        onOpenLogService = { service, range -> backStack.add(LogServiceNavKey(service, range)) },
        onOpenExperiment = { backStack.add(ExperimentDetailNavKey(it)) },
    )
    entry<HomeNavKey> {
        HomeRoute(
            initialNavigationPreferences = initialNavigationPreferences,
            onNavigateToSettings = { backStack.add(SettingsNavKey) },
            onOpenSection = { backStack.add(HomeSectionNavKey(it)) },
            navigation = sectionNavigation,
        )
    }
    entry<HomeSectionNavKey> { key ->
        HomeSectionRoute(section = key.section, onNavigateBack = back, navigation = sectionNavigation)
    }
}

private fun EntryProviderScope<NavKey>.settingsEntries(backStack: NavBackStack<NavKey>, back: () -> Unit) {
    entry<SettingsNavKey> {
        SettingsRoute(
            onNavigateBack = back,
            onOpenNavigationSettings = { backStack.add(NavigationSettingsNavKey) },
            onOpenAnalyticsSettings = { backStack.add(AnalyticsSettingsNavKey) },
            onOpenPrivacy = { backStack.add(PrivacySettingsNavKey) },
            onOpenLibraries = { backStack.add(LibrariesNavKey) },
        )
    }
    entry<NavigationSettingsNavKey> { NavigationSettingsRoute(onNavigateBack = back) }
    entry<AnalyticsSettingsNavKey> { AnalyticsSettingsRoute(onNavigateBack = back) }
    entry<PrivacySettingsNavKey> { PrivacySettingsRoute(onNavigateBack = back) }
    entry<LibrariesNavKey> {
        LibrariesRoute(onNavigateBack = back, onOpenLibrary = { backStack.add(LibraryDetailNavKey(it)) })
    }
    entry<LibraryDetailNavKey> { key -> LibraryDetailRoute(navKey = key, onNavigateBack = back) }
}

/** A single dashboard, insight, flag, issue or experiment, opened from a list or a link. */
private fun EntryProviderScope<NavKey>.detailEntries(backStack: NavBackStack<NavKey>, back: () -> Unit) {
    entry<DashboardDetailNavKey> { key ->
        DashboardDetailRoute(
            navKey = key,
            onNavigateBack = back,
            onOpenInsight = { backStack.add(InsightDetailNavKey(it)) },
        )
    }
    entry<InsightDetailNavKey> { key -> InsightDetailRoute(navKey = key, onNavigateBack = back) }
    entry<FeatureFlagDetailNavKey> { key -> FeatureFlagDetailRoute(navKey = key, onNavigateBack = back) }
    entry<ErrorTrackingIssueNavKey> { key -> ErrorTrackingIssueRoute(navKey = key, onNavigateBack = back) }
    entry<ExperimentDetailNavKey> { key ->
        ExperimentDetailRoute(
            navKey = key,
            onNavigateBack = back,
            onOpenFeatureFlag = { backStack.add(FeatureFlagDetailNavKey(it)) },
        )
    }
}

/** A service's log lines and a single line, both pushed from the Logs section. */
private fun EntryProviderScope<NavKey>.logsEntries(backStack: NavBackStack<NavKey>, back: () -> Unit) {
    entry<LogServiceNavKey> { key ->
        LogServiceRoute(
            navKey = key,
            onNavigateBack = back,
            onOpenEntry = { entryId, timestamp -> backStack.add(LogEntryNavKey(key.service, entryId, timestamp)) },
        )
    }
    entry<LogEntryNavKey> { key -> LogEntryRoute(navKey = key, onNavigateBack = back) }
}
