package com.nightcode.snout.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.home.HomeRoute
import com.nightcode.snout.features.intialproject.InitialProjectNavKey
import com.nightcode.snout.features.intialproject.InitialProjectRoute
import com.nightcode.snout.features.login.LoginNavKey
import com.nightcode.snout.features.login.LoginRoute

@Composable
internal fun MainNavGraph(initial: NavKey) {
    val backStack =
        rememberNavBackStack(DefaultSavedStateConfiguration, initial, InitialProjectNavKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = entryProvider {
            entry<HomeNavKey> { HomeRoute() }
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
        },
    )
}
