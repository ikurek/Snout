package com.nightcode.snout.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.features.projectswitcher.ProjectSwitcherSheet
import com.nightcode.snout.features.projectswitcher.ProjectSwitcherViewModel
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.telemetry.ProjectSelectionSource
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryScreen
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackScreenViewed
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun HomeRoute(
    initialNavigationPreferences: NavigationPreferences,
    onNavigateToSettings: () -> Unit,
    onOpenSection: (HomeSection) -> Unit,
    navigation: SectionNavigation,
) {
    val viewModel = koinViewModel<HomeViewModel> { parametersOf(initialNavigationPreferences) }
    val trackScreenViewed = koinInject<TrackScreenViewed>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val switcherViewModel = koinViewModel<ProjectSwitcherViewModel>()
    val switcherState by switcherViewModel.uiState.collectAsStateWithLifecycle()
    var showProjectSwitcher by rememberSaveable { mutableStateOf(false) }

    HomeScreen(
        preferences = state.navigationPreferences,
        currentProjectName = switcherState.currentProjectName,
        onOpenProjectSwitcher = { showProjectSwitcher = true },
        onNavigateToSettings = onNavigateToSettings,
        onOpenSection = onOpenSection,
        onOpenInPostHog = rememberPostHogOpener(),
        navigation = navigation,
        onSectionShown = { section -> trackScreenViewed(section?.let(TelemetryScreen::of) ?: TelemetryScreen.More) },
    )

    ProjectSwitcherSheet(
        isVisible = showProjectSwitcher,
        state = switcherState,
        onProjectSelected = { organizationId, projectId ->
            switcherViewModel.onProjectSelected(organizationId, projectId, ProjectSelectionSource.Home)
        },
        onDismiss = { showProjectSwitcher = false },
    )
}
