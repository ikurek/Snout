package com.nightcode.snout.features.logs.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.navigation.LogServiceNavKey
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Instant

@Composable
internal fun LogServiceRoute(
    navKey: LogServiceNavKey,
    onNavigateBack: () -> Unit,
    onOpenEntry: (entryId: String, timestamp: Instant) -> Unit,
) {
    val viewModel = koinViewModel<LogServiceViewModel> { parametersOf(navKey) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)
    val openInPostHog = rememberPostHogOpener()

    LogServiceScreen(
        service = navKey.service,
        range = navKey.range,
        state = state,
        onNavigateBack = onNavigateBack,
        onQueryChanged = viewModel::onQueryChanged,
        onSeverityChanged = viewModel::onSeverityChanged,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
        onOpenEntry = { onOpenEntry(it.id, it.timestamp) },
        onOpenInPostHog = { openInPostHog(PostHogResource.Logs) },
    )
}
