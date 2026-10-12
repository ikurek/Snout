package com.nightcode.snout.features.logs.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Instant

@Composable
internal fun LogEntryRoute(navKey: LogEntryNavKey, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<LogEntryViewModel> { parametersOf(navKey) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LogEntryScreen(
        service = navKey.service,
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onOpenInPostHog = rememberOpenInPostHog(PostHogResource.Logs),
    )
}

/** A log line; [service] and [timestamp] let it be found again, since PostHog can't fetch one line by [id]. */
@Serializable
internal data class LogEntryNavKey(val service: String, val id: String, val timestamp: Instant) : NavKey
