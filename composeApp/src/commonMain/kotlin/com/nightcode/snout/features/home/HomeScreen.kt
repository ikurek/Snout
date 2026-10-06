package com.nightcode.snout.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton
import com.nightcode.snout.designsystem.organisms.BottomNavigation
import com.nightcode.snout.designsystem.organisms.BottomNavigationItem
import com.nightcode.snout.designsystem.organisms.SnoutTopAppBar
import com.nightcode.snout.features.analytics.AnalyticsRoute
import com.nightcode.snout.features.dashboards.DashboardsRoute
import com.nightcode.snout.features.featureflags.FeatureFlagsRoute
import com.nightcode.snout.features.insights.InsightsRoute
import com.nightcode.snout.features.more.MoreItem
import com.nightcode.snout.features.more.MoreScreen
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_settings_24dp
import snout.composeapp.generated.resources.settings_icon_content_description

@Composable
internal fun HomeScreen(
    currentProjectName: String,
    onOpenProjectSwitcher: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenDashboard: (dashboardId: Long) -> Unit,
    onOpenInsight: (insightId: Long) -> Unit,
    onOpenFeatureFlag: (flagId: Long) -> Unit,
    onMoreItemClick: (MoreItem) -> Unit,
    onOpenInPostHog: (PostHogResource) -> Unit,
) {
    var currentDestination by rememberSaveable { mutableStateOf(HomeDestination.Analytics) }

    Scaffold(
        topBar = {
            SnoutTopAppBar(
                title = stringResource(currentDestination.label),
                navigationIcon = {
                    IconButton(onClick = onOpenProjectSwitcher, modifier = Modifier.size(48.dp)) {
                        Crossfade(targetState = currentProjectName) { AvatarBox(name = it) }
                    }
                },
                actions = {
                    OpenInPostHogIconButton(onClick = { onOpenInPostHog(currentDestination.resource) })
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
            BottomNavigation(
                items = HomeDestination.entries.map {
                    BottomNavigationItem(label = stringResource(it.label), icon = painterResource(it.icon))
                },
                selectedIndex = currentDestination.ordinal,
                onSelect = { currentDestination = HomeDestination.entries[it] },
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentDestination,
            label = "HomeTabContent",
            modifier = Modifier.padding(innerPadding),
        ) { destination ->
            when (destination) {
                HomeDestination.Analytics -> AnalyticsRoute()
                HomeDestination.Dashboards -> DashboardsRoute(onOpenDashboard = onOpenDashboard)
                HomeDestination.Insights -> InsightsRoute(onOpenInsight = onOpenInsight)
                HomeDestination.FeatureFlags -> FeatureFlagsRoute(onOpenFlag = onOpenFeatureFlag)
                HomeDestination.More -> MoreScreen(onItemClick = onMoreItemClick)
            }
        }
    }
}
