package com.nightcode.snout.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
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
import com.nightcode.snout.features.intialproject.InitialProjectNavKey
import com.nightcode.snout.features.intialproject.InitialProjectRoute
import com.nightcode.snout.features.login.LoginNavKey
import com.nightcode.snout.features.login.LoginRoute
import com.nightcode.snout.features.settings.SettingsNavKey
import com.nightcode.snout.features.settings.SettingsRoute
import com.nightcode.snout.features.settings.analytics.AnalyticsSettingsNavKey
import com.nightcode.snout.features.settings.analytics.AnalyticsSettingsRoute
import com.nightcode.snout.features.settings.navigation.NavigationSettingsNavKey
import com.nightcode.snout.features.settings.navigation.NavigationSettingsRoute
import com.nightcode.snout.shared.domain.usecase.auth.ClearLocalData
import com.nightcode.snout.shared.domain.usecase.auth.ShouldReturnToLogin
import org.koin.compose.koinInject

private val PushTransition: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
}

private val PopTransition: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
}

@Composable
internal fun MainNavGraph(initial: NavKey) {
    val backStack = rememberNavBackStack(DefaultSavedStateConfiguration, initial)
    ReturnToLoginOnSessionExpiry(backStack)

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
        entryProvider = mainEntryProvider(backStack),
    )
}

/** Wipes local data and restarts at login once the API key disappears (logout or 401). */
@Composable
private fun ReturnToLoginOnSessionExpiry(backStack: NavBackStack<NavKey>) {
    val shouldReturnToLogin = koinInject<ShouldReturnToLogin>()
    val clearLocalData = koinInject<ClearLocalData>()

    LaunchedEffect(Unit) {
        shouldReturnToLogin().collect {
            if (backStack.contains(LoginNavKey).not()) {
                clearLocalData()
                backStack.clear()
                backStack.add(LoginNavKey)
            }
        }
    }
}

private fun mainEntryProvider(backStack: NavBackStack<NavKey>): (NavKey) -> NavEntry<NavKey> {
    val back: () -> Unit = { backStack.removeLastOrNull() }
    val sectionNavigation = SectionNavigation(
        onOpenDashboard = { backStack.add(DashboardDetailNavKey(it)) },
        onOpenInsight = { backStack.add(InsightDetailNavKey(it)) },
        onOpenFeatureFlag = { backStack.add(FeatureFlagDetailNavKey(it)) },
        onOpenExperiment = { backStack.add(ExperimentDetailNavKey(it)) },
    )
    return entryProvider {
        entry<LoginNavKey> { LoginRoute(onUserAuthorized = { backStack.add(InitialProjectNavKey) }) }
        entry<InitialProjectNavKey> {
            InitialProjectRoute(
                onProjectSelected = {
                    backStack.clear()
                    backStack.add(HomeNavKey)
                },
            )
        }
        entry<HomeNavKey> {
            HomeRoute(
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
        entry<ExperimentDetailNavKey> { key ->
            ExperimentDetailRoute(
                navKey = key,
                onNavigateBack = back,
                onOpenFeatureFlag = { backStack.add(FeatureFlagDetailNavKey(it)) },
            )
        }
    }
}
