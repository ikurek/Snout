package com.nightcode.snout.features.logs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LogsRoute(
    onOpenService: (service: String, range: LogsRange) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModel = koinViewModel<LogsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LogsScreen(
        state = state,
        onRangeChange = viewModel::onRangeChange,
        onRefresh = viewModel::refresh,
        onOpenService = { onOpenService(it, state.range) },
        contentPadding = contentPadding,
    )
}
