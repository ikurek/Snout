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
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.refreshFailedMessage
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.stringResource

/*
 * The pieces a screen showing fetched data picks from, depending on its own state: nothing loaded yet
 * ([LoadingOrErrorContent]), loaded but empty ([RefreshableEmptyContent]), or loaded ([RefreshableContent]).
 * Loading is skeleton rows, never a spinner; a failed reload keeps what was loaded under an inline banner.
 */

/** Skeleton rows for a detail screen's first load. */
internal const val DetailLoadingRowCount = 4

/**
 * What shows before anything is loaded: [loadingContent] while the first load runs ([error] is null), then the
 * error placeholder with retry. [notFoundContent] replaces the placeholder when the API answered 404, for sections an
 * instance may lack altogether (usually an [UnavailablePlaceholder]).
 */
@Composable
internal fun LoadingOrErrorContent(
    error: DataError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { SkeletonRows() },
    notFoundContent: (@Composable () -> Unit)? = null,
) {
    when {
        error == null -> Box(modifier = modifier.fillMaxSize()) { loadingContent() }

        error == DataError.NotFound && notFoundContent != null ->
            Box(modifier = modifier.fillMaxSize()) { notFoundContent() }

        else -> ErrorPlaceholder(error = error, onRetry = onRetry, modifier = modifier.fillMaxSize())
    }
}

/**
 * Loaded [content] under the refresh indicator, with [refreshError] as a banner over it when the last reload failed.
 * Detail screens turn the pull gesture off ([isPullToRefreshEnabled]): they refresh when entered again or retried,
 * and the indicator only shows a load running.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RefreshableContent(
    isRefreshing: Boolean,
    refreshError: DataError?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isPullToRefreshEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        enabled = isPullToRefreshEnabled,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            refreshError?.let { error ->
                InlineErrorBanner(
                    text = stringResource(error.refreshFailedMessage()),
                    modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
                )
            }
            content()
        }
    }
}

/** A load that found nothing: [content] (usually a placeholder) centred in the viewport, with pull to refresh. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RefreshableEmptyContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        NothingFoundPlaceholder(
            onRetry = onRefresh,
            modifier = Modifier.fillMaxSize(),
        )
    },
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
        // A scrollable child is needed for the pull gesture; size it to the viewport so content stays centered.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val viewportHeight = maxHeight
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().height(viewportHeight)) { content() }
            }
        }
    }
}
