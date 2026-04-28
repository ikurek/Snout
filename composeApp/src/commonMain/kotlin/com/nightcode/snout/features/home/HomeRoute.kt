package com.nightcode.snout.features.home

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object HomeNavKey : NavKey

@Composable
internal fun HomeRoute() {
    val viewModel = koinViewModel<HomeViewModel>()

    HomeScreen(
        greeting = viewModel.greeting,
    )
}
