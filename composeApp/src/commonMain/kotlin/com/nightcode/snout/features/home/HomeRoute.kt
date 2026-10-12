package com.nightcode.snout.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.links.SubmitIncomingLink
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeRoute(
    initialPreferences: NavigationPreferences?,
    onNavigateToSettings: () -> Unit,
    onOpenSection: (HomeSection) -> Unit,
    navigation: SectionNavigation,
) {
    val viewModel = koinViewModel<ProjectSwitcherViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showProjectSwitcher by rememberSaveable { mutableStateOf(false) }
    var showOpenLink by rememberSaveable { mutableStateOf(false) }
    val submitIncomingLink = koinInject<SubmitIncomingLink>()
    val scope = rememberCoroutineScope()

    val observeNavigationPreferences = koinInject<ObserveNavigationPreferences>()
    val preferences by observeNavigationPreferences().collectAsStateWithLifecycle(initialValue = initialPreferences)

    // The bar and the landing tab come from preferences. On app start they're read up front; when Home is
    // reached later (after login) render nothing for the first few milliseconds rather than flash the defaults.
    val loaded: NavigationPreferences = preferences ?: return

    HomeScreen(
        preferences = loaded,
        currentProjectName = uiState.currentProjectName,
        onOpenProjectSwitcher = { showProjectSwitcher = true },
        onNavigateToSettings = onNavigateToSettings,
        onOpenLink = { showOpenLink = true },
        onOpenSection = onOpenSection,
        onOpenInPostHog = rememberPostHogOpener(),
        navigation = navigation,
    )

    ProjectSwitcherBottomSheet(
        isVisible = showProjectSwitcher,
        state = uiState,
        onProjectSelected = viewModel::onProjectSelected,
        onDismiss = { showProjectSwitcher = false },
    )

    OpenLinkSheet(
        isVisible = showOpenLink,
        onDismiss = { showOpenLink = false },
        onOpen = { url ->
            showOpenLink = false
            scope.launch { submitIncomingLink(url) }
        },
    )
}

@Serializable
internal data object HomeNavKey : NavKey
