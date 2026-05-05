package com.nightcode.snout.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nightcode.snout.features.analytics.AnalyticsRoute
import com.nightcode.snout.features.dashboard.DashboardRoute
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_settings_24dp
import snout.composeapp.generated.resources.settings_icon_content_description

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(onNavigateToSettings: () -> Unit) {
    var currentDestination by rememberSaveable { mutableStateOf(HomeDestination.Dashboard) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(currentDestination.label)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_settings_24dp),
                            contentDescription = stringResource(Res.string.settings_icon_content_description),
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                HomeDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == currentDestination,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                painter = painterResource(destination.icon),
                                contentDescription = stringResource(destination.label),
                            )
                        },
                        label = { Text(stringResource(destination.label)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentDestination,
            label = "HomeTabContent",
            modifier = Modifier.padding(innerPadding),
        ) { destination ->
            when (destination) {
                HomeDestination.Dashboard -> DashboardRoute()
                HomeDestination.Analytics -> AnalyticsRoute()
            }
        }
    }
}
