package com.nightcode.snout.features.home

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.features.projectswitcher.ProjectSwitcherSheet
import com.nightcode.snout.features.projectswitcher.ProjectSwitcherViewModel
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeRoute(
    onNavigateToSettings: () -> Unit,
    onOpenSection: (HomeSection) -> Unit,
    navigation: SectionNavigation,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val switcherViewModel = koinViewModel<ProjectSwitcherViewModel>()
    val switcherState by switcherViewModel.uiState.collectAsStateWithLifecycle()
    var showProjectSwitcher by rememberSaveable { mutableStateOf(false) }
    var showOpenLink by rememberSaveable { mutableStateOf(false) }
    val linkState = rememberTextFieldState()
    // The open-link sheet's error: its link was turned down, until the text changes.
    var linkRejected by remember { mutableStateOf(false) }
    LaunchedEffect(linkState) {
        snapshotFlow { linkState.text.toString() }.collect { linkRejected = false }
    }

    // The bar and the landing tab come from preferences: draw only the background for the few milliseconds they
    // take to read rather than flash the defaults.
    val preferences = state.navigationPreferences ?: return

    HomeScreen(
        preferences = preferences,
        currentProjectName = switcherState.currentProjectName,
        onOpenProjectSwitcher = { showProjectSwitcher = true },
        onNavigateToSettings = onNavigateToSettings,
        onOpenLink = { showOpenLink = true },
        onOpenSection = onOpenSection,
        onOpenInPostHog = rememberPostHogOpener(),
        navigation = navigation,
    )

    ProjectSwitcherSheet(
        isVisible = showProjectSwitcher,
        state = switcherState,
        onProjectSelected = switcherViewModel::onProjectSelected,
        onDismiss = { showProjectSwitcher = false },
    )

    OpenLinkSheet(
        isVisible = showOpenLink,
        linkState = linkState,
        showError = linkRejected,
        onDismiss = { showOpenLink = false },
        onOpen = {
            if (viewModel.onLinkSubmitted(linkState.text.toString())) {
                showOpenLink = false
                linkState.clearText()
            } else {
                linkRejected = true
            }
        },
    )
}
