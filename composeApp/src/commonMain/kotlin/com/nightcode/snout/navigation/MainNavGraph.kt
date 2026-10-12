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
import com.nightcode.snout.features.dashboards.detail.DashboardDetailNavKey
import com.nightcode.snout.features.dashboards.detail.DashboardDetailRoute
import com.nightcode.snout.features.errortracking.detail.ErrorTrackingIssueNavKey
import com.nightcode.snout.features.errortracking.detail.ErrorTrackingIssueRoute
import com.nightcode.snout.features.experiments.detail.ExperimentDetailNavKey
import com.nightcode.snout.features.experiments.detail.ExperimentDetailRoute
import com.nightcode.snout.features.featureflags.detail.FeatureFlagDetailNavKey
import com.nightcode.snout.features.featureflags.detail.FeatureFlagDetailRoute
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.home.HomeRoute
import com.nightcode.snout.features.home.SectionNavKey
import com.nightcode.snout.features.home.SectionNavigation
import com.nightcode.snout.features.home.SectionRoute
import com.nightcode.snout.features.insights.detail.InsightDetailNavKey
import com.nightcode.snout.features.insights.detail.InsightDetailRoute
import com.nightcode.snout.features.links.toNavKey
import com.nightcode.snout.features.logs.entry.LogEntryNavKey
import com.nightcode.snout.features.logs.entry.LogEntryRoute
import com.nightcode.snout.features.logs.service.LogServiceNavKey
import com.nightcode.snout.features.logs.service.LogServiceRoute
import com.nightcode.snout.features.onboarding.OnboardingNavKey
import com.nightcode.snout.features.onboarding.OnboardingRoute
import com.nightcode.snout.features.settings.SettingsNavKey
import com.nightcode.snout.features.settings.SettingsRoute
import com.nightcode.snout.features.settings.analytics.AnalyticsSettingsNavKey
import com.nightcode.snout.features.settings.analytics.AnalyticsSettingsRoute
import com.nightcode.snout.features.settings.navigation.NavigationSettingsNavKey
import com.nightcode.snout.features.settings.navigation.NavigationSettingsRoute
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.links.LinkResolution
import com.nightcode.snout.shared.domain.usecase.appstate.DetermineInitialLocation
import com.nightcode.snout.shared.domain.usecase.auth.ClearLocalData
import com.nightcode.snout.shared.domain.usecase.auth.ShouldReturnToLogin
import com.nightcode.snout.shared.domain.usecase.links.ClearPendingLink
import com.nightcode.snout.shared.domain.usecase.links.ObservePendingLink
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import com.nightcode.snout.shared.domain.usecase.links.ResolvePostHogLink
import kotlinx.coroutines.flow.filterNotNull
import org.koin.compose.koinInject

private val PushTransition: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
}

private val PopTransition: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
}

@Composable
internal fun MainNavGraph(initial: NavKey, initialNavigationPreferences: NavigationPreferences? = null) {
    val backStack = rememberNavBackStack(DefaultSavedStateConfiguration, initial)
    ReturnToLoginOnSessionExpiry(backStack)
    HandleIncomingLinks(backStack)

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
    val hasHome = backStack.contains(HomeNavKey)

    LaunchedEffect(hasHome) {
        if (!hasHome) return@LaunchedEffect
        observePendingLink().filterNotNull().collect { url ->
            clearPendingLink()
            when (val resolution = resolvePostHogLink(url)) {
                is LinkResolution.InApp -> {
                    val home = backStack.indexOf(HomeNavKey)
                    repeat(backStack.lastIndex - home) { backStack.removeAt(backStack.lastIndex) }
                    resolution.destination.toNavKey()?.let(backStack::add)
                }

                is LinkResolution.Browser -> openInBrowser(resolution.url)
            }
        }
    }
}

private fun mainEntryProvider(
    backStack: NavBackStack<NavKey>,
    initialNavigationPreferences: NavigationPreferences?,
): (NavKey) -> NavEntry<NavKey> {
    val back: () -> Unit = { backStack.removeLastOrNull() }
    val sectionNavigation = SectionNavigation(
        onOpenDashboard = { backStack.add(DashboardDetailNavKey(it)) },
        onOpenInsight = { backStack.add(InsightDetailNavKey(it)) },
        onOpenFeatureFlag = { backStack.add(FeatureFlagDetailNavKey(it)) },
        onOpenErrorTrackingIssue = { backStack.add(ErrorTrackingIssueNavKey(it)) },
        onOpenLogService = { service, range -> backStack.add(LogServiceNavKey(service, range)) },
        onOpenExperiment = { backStack.add(ExperimentDetailNavKey(it)) },
    )
    return entryProvider {
        entry<OnboardingNavKey> { key ->
            OnboardingRoute(
                navKey = key,
                onFinished = {
                    backStack.clear()
                    backStack.add(HomeNavKey)
                },
            )
        }
        entry<HomeNavKey> {
            HomeRoute(
                initialPreferences = initialNavigationPreferences,
                onNavigateToSettings = { backStack.add(SettingsNavKey) },
                onOpenSection = { backStack.add(SectionNavKey(it)) },
                navigation = sectionNavigation,
            )
        }
        entry<SectionNavKey> { key ->
            SectionRoute(section = key.section, onNavigateBack = back, navigation = sectionNavigation)
        }
        entry<SettingsNavKey> {
            SettingsRoute(
                onNavigateBack = back,
                onOpenNavigationSettings = { backStack.add(NavigationSettingsNavKey) },
                onOpenAnalyticsSettings = { backStack.add(AnalyticsSettingsNavKey) },
            )
        }
        entry<NavigationSettingsNavKey> { NavigationSettingsRoute(onNavigateBack = back) }
        entry<AnalyticsSettingsNavKey> { AnalyticsSettingsRoute(onNavigateBack = back) }
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
        logsEntries(backStack, back)
        entry<ExperimentDetailNavKey> { key ->
            ExperimentDetailRoute(
                navKey = key,
                onNavigateBack = back,
                onOpenFeatureFlag = { backStack.add(FeatureFlagDetailNavKey(it)) },
            )
        }
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
