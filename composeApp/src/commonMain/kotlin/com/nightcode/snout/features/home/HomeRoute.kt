package com.nightcode.snout.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object HomeNavKey : NavKey

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
}
