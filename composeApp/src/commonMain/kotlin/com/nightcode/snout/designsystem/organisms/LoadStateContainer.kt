package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.designsystem.atoms.SkeletonRows

private const val LoadingRowCount = 4

/**
 * Renders a one-shot fetch: skeleton rows while loading, an error placeholder with retry, then [content] filling
 * the container. The loading and error states match [CachedStateContainer]'s.
 */
@Composable
internal fun <T> LoadStateContainer(
    state: LoadState<T>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { SkeletonRows(count = LoadingRowCount) },
    content: @Composable (data: T) -> Unit,
) {
    when (state) {
        LoadState.Loading -> Box(modifier = modifier.fillMaxSize()) { loadingContent() }

        is LoadState.Error ->
            ErrorPlaceholder(error = state.error, onRetry = onRefresh, modifier = modifier.fillMaxSize())

        is LoadState.Loaded -> Box(modifier = modifier.fillMaxSize()) { content(state.data) }
    }
}
