package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.molecules.InlineError
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.refresh_failed_forbidden
import snout.composeapp.generated.resources.refresh_failed_generic
import snout.composeapp.generated.resources.refresh_failed_offline
import snout.composeapp.generated.resources.refresh_failed_rate_limited

/** Renders loading, error, empty and content states of cache-first data with pull to refresh. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> CachedStateContainer(
    state: CachedUiState<T>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable () -> Unit = {
        NothingFoundPlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
    },
    content: @Composable (data: T) -> Unit,
) {
    when (state) {
        CachedUiState.Loading -> LoadingIndicator(modifier)

        is CachedUiState.Error ->
            ErrorPlaceholder(error = state.error, onRetry = onRefresh, modifier = modifier.fillMaxSize())

        is CachedUiState.Empty -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { emptyContent() }
        }

        is CachedUiState.Content -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                state.refreshError?.let { error ->
                    InlineError(
                        text = stringResource(error.refreshMessage()),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                content(state.data)
            }
        }
    }
}

@Composable
internal fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private fun DataError.refreshMessage() = when (this) {
    DataError.Forbidden -> Res.string.refresh_failed_forbidden
    DataError.RateLimited -> Res.string.refresh_failed_rate_limited
    DataError.ConnectionError -> Res.string.refresh_failed_offline
    DataError.NotFound, DataError.UnexpectedError -> Res.string.refresh_failed_generic
}
