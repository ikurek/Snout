package com.nightcode.snout.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.shared.domain.model.common.DataError

@Composable
internal fun ErrorPlaceholder(error: DataError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (error) {
        DataError.Forbidden -> PermissionDeniedPlaceholder(onRetry = onRetry, modifier = modifier)

        DataError.RateLimited -> RateLimitedPlaceholder(onRetry = onRetry, modifier = modifier)

        DataError.NotFound -> NothingFoundPlaceholder(onRetry = onRetry, modifier = modifier)

        DataError.ConnectionError, DataError.UnexpectedError -> GenericErrorPlaceholder(
            onRetry = onRetry,
            modifier = modifier,
        )
    }
}
