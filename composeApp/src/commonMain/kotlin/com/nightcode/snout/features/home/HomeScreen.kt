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
import com.nightcode.snout.common.sections.icon
import com.nightcode.snout.common.sections.label
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton
import com.nightcode.snout.designsystem.organisms.BottomNavigation
import com.nightcode.snout.designsystem.organisms.BottomNavigationItem
import com.nightcode.snout.designsystem.organisms.SnoutTopAppBar
import com.nightcode.snout.features.more.MoreScreen
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import com.nightcode.snout.shared.domain.model.links.resource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.bottom_nav_more
import snout.composeapp.generated.resources.ic_link_24dp
import snout.composeapp.generated.resources.ic_more_horiz_24dp
import snout.composeapp.generated.resources.ic_settings_24dp
import snout.composeapp.generated.resources.open_link_icon_content_description
import snout.composeapp.generated.resources.settings_icon_content_description

/** The home shell: four preference-driven tabs plus More. `null` as the current section means More. */
@Composable
internal fun HomeScreen(
    preferences: NavigationPreferences,
    currentProjectName: String,
    onOpenProjectSwitcher: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenLink: () -> Unit,
    onOpenSection: (HomeSection) -> Unit,
    onOpenInPostHog: (PostHogResource) -> Unit,
    navigation: SectionNavigation,
) {
    var selected by rememberSaveable { mutableStateOf<HomeSection?>(preferences.landing) }
    // A tab removed in settings while it was open falls back to More rather than showing a bar with nothing lit.
    val current = selected?.takeIf { it in preferences.tabs }
    val title = current?.let { stringResource(it.label) } ?: stringResource(Res.string.bottom_nav_more)

    Scaffold(
        topBar = {
            SnoutTopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onOpenProjectSwitcher, modifier = Modifier.size(48.dp)) {
                        Crossfade(targetState = currentProjectName) { AvatarBox(name = it) }
                    }
                },
                actions = {
                    OpenInPostHogIconButton(
                        onClick = { onOpenInPostHog(current?.resource ?: PostHogResource.Project) },
                    )
                    IconButton(onClick = onOpenLink) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_link_24dp),
                            contentDescription = stringResource(Res.string.open_link_icon_content_description),
                        )
                    }
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
                items = preferences.tabs.map {
                    BottomNavigationItem(label = stringResource(it.label), icon = painterResource(it.icon))
                } + BottomNavigationItem(
                    label = stringResource(Res.string.bottom_nav_more),
                    icon = painterResource(Res.drawable.ic_more_horiz_24dp),
                ),
                selectedIndex = current?.let { preferences.tabs.indexOf(it) } ?: preferences.tabs.size,
                onSelect = { index -> selected = preferences.tabs.getOrNull(index) },
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = current,
            label = "HomeTabContent",
            modifier = Modifier.padding(innerPadding),
        ) { section ->
            if (section == null) {
                MoreScreen(sections = preferences.hidden, onItemClicked = onOpenSection)
            } else {
                HomeSectionContent(section = section, navigation = navigation)
            }
        }
    }
}
