package com.nightcode.snout.features.persons.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.navigation.PersonDetailNavKey
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun PersonDetailRoute(
    navKey: PersonDetailNavKey,
    onNavigateBack: () -> Unit,
    onOpenIssue: (issueId: String) -> Unit,
) {
    val viewModel = koinViewModel<PersonDetailViewModel> { parametersOf(navKey) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)
    val openInPostHog = rememberPostHogOpener()

    PersonDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onTabSelected = viewModel::onTabSelected,
        onEventsEndReached = viewModel::onEventsEndReached,
        onOpenIssue = onOpenIssue,
        onOpenInPostHog = { openInPostHog(PostHogResource.Person(navKey.uuid)) },
    )
}
