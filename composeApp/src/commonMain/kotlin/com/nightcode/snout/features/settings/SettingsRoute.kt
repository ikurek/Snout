package com.nightcode.snout.features.settings

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal data object SettingsNavKey : NavKey

@Composable
internal fun SettingsRoute(onNavigateBack: () -> Unit) {
    SettingsScreen(onNavigateBack = onNavigateBack)
}
