package com.nightcode.snout.features.logs.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import kotlinx.serialization.Serializable
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
        onOpenInPostHog = rememberOpenInPostHog(PostHogResource.Logs),
    )
}

/** A service's log lines over the [range] picked on the Logs screen. */
@Serializable
internal data class LogServiceNavKey(val service: String, val range: LogsRange) : NavKey
