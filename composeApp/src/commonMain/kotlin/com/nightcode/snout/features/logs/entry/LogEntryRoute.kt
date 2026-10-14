package com.nightcode.snout.features.logs.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.navigation.LogEntryNavKey
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun LogEntryRoute(navKey: LogEntryNavKey, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<LogEntryViewModel> { parametersOf(navKey) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)
    val openInPostHog = rememberPostHogOpener()

    LogEntryScreen(
        service = navKey.service,
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onOpenInPostHog = { openInPostHog(PostHogResource.Logs) },
    )
}
