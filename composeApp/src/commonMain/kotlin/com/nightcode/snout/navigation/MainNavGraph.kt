package com.nightcode.snout.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.home.HomeRoute
import com.nightcode.snout.features.intialproject.InitialProjectNavKey
import com.nightcode.snout.features.intialproject.InitialProjectRoute
import com.nightcode.snout.features.login.LoginNavKey
import com.nightcode.snout.features.login.LoginRoute
import com.nightcode.snout.features.settings.SettingsNavKey
import com.nightcode.snout.features.settings.SettingsRoute
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
    val backStack =
        rememberNavBackStack(DefaultSavedStateConfiguration, initial)
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
        entryProvider = entryProvider {
            entry<HomeNavKey> {
                HomeRoute(
                    onNavigateToSettings = { backStack.add(SettingsNavKey) },
                    onMoreItemClick = { },
                )
            }
            entry<LoginNavKey> {
                LoginRoute(
                    onUserAuthorized = { backStack.add(InitialProjectNavKey) },
                )
            }
            entry<InitialProjectNavKey> {
                InitialProjectRoute(
                    onProjectSelected = {
                        backStack.clear()
                        backStack.add(HomeNavKey)
                    },
                )
            }
            entry<SettingsNavKey> {
                SettingsRoute(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
