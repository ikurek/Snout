package com.nightcode.snout.features.home

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal data object HomeNavKey : NavKey

@Composable
internal fun HomeRoute(onNavigateToSettings: () -> Unit) {
    HomeScreen(onNavigateToSettings = onNavigateToSettings)
}
