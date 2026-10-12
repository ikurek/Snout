package com.nightcode.snout.features.home

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.HoldSplash
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

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                HomeSideEffect.LinkAccepted -> {
                    showOpenLink = false
                    linkState.clearText()
                }
            }
        }
    }

    // The bar and the landing tab come from preferences: draw nothing for the few milliseconds they take to read
    // rather than flash the defaults, and on app start keep the splash up meanwhile.
    val preferences = state.navigationPreferences
    if (preferences == null) {
        HoldSplash()
        return
    }

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
        showError = state.rejectedLink != null && state.rejectedLink == linkState.text.toString(),
        onDismiss = { showOpenLink = false },
        onOpen = { viewModel.onLinkSubmitted(linkState.text.toString()) },
    )
}
