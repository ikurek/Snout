package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.load_more_retry

private const val LoadingMoreRows = 2

/** Loading the page after the ones a list shows. */
internal sealed interface LoadMoreStatus {
    /** More pages exist and none is loading. */
    data object Idle : LoadMoreStatus

    data object Loading : LoadMoreStatus

    data class Failed(val error: DataError) : LoadMoreStatus

    /** The last page is on screen. */
    data object Done : LoadMoreStatus
}

/**
 * The last item of a paged list. Composing it means the list reached its end, which is the cue to fetch the next
 * page; a failed page says [failedText] with a retry.
 */
@Composable
internal fun LoadMoreFooter(status: LoadMoreStatus, failedText: String, onLoadMore: () -> Unit) {
    when (status) {
        LoadMoreStatus.Idle -> LaunchedEffect(Unit) { onLoadMore() }

        LoadMoreStatus.Loading -> SkeletonRows(count = LoadingMoreRows)

        is LoadMoreStatus.Failed -> InlineErrorBanner(
            text = failedText,
            actionLabel = stringResource(Res.string.load_more_retry),
            onAction = onLoadMore,
            modifier = Modifier.padding(Spacing.large),
        )

        LoadMoreStatus.Done -> Unit
    }
}
