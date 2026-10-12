package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.refreshFailedMessage
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.stringResource

/**
 * Renders loading, error, empty and content states of data that reloads, with pull to refresh.
 * Loading is skeleton rows, never a spinner; a failed reload keeps the loaded content under an inline banner.
 * [notFoundContent] replaces the error placeholder when the API answered 404, for sections an instance may lack
 * altogether (usually an [UnavailablePlaceholder]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> RefreshableStateContainer(
    state: RefreshableUiState<T>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { SkeletonRows() },
    emptyContent: @Composable () -> Unit = {
        NothingFoundPlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
    },
    notFoundContent: (@Composable () -> Unit)? = null,
    content: @Composable (data: T) -> Unit,
) {
    when (state) {
        RefreshableUiState.Loading -> Box(modifier = modifier.fillMaxSize()) { loadingContent() }

        is RefreshableUiState.Error -> if (state.error == DataError.NotFound && notFoundContent != null) {
            Box(modifier = modifier.fillMaxSize()) { notFoundContent() }
        } else {
            ErrorPlaceholder(error = state.error, onRetry = onRefresh, modifier = modifier.fillMaxSize())
        }

        is RefreshableUiState.Empty -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize(),
        ) {
            // A scrollable child is needed for the pull gesture; size it to the viewport so content stays centered.
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val viewportHeight = maxHeight
                Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.fillMaxWidth().height(viewportHeight)) { emptyContent() }
                }
            }
        }

        is RefreshableUiState.Content -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                state.refreshError?.let { error ->
                    InlineErrorBanner(
                        text = stringResource(error.refreshFailedMessage()),
                        modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
                    )
                }
                content(state.data)
            }
        }
    }
}
