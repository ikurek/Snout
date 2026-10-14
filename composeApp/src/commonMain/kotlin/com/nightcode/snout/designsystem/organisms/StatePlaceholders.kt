package com.nightcode.snout.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_credit_card_24dp
import snout.composeapp.generated.resources.ic_info_outlined_24dp
import snout.composeapp.generated.resources.ic_lock_outlined_24dp
import snout.composeapp.generated.resources.ic_schedule_24dp
import snout.composeapp.generated.resources.ic_search_24dp
import snout.composeapp.generated.resources.ic_warning_outlined_24dp
import snout.composeapp.generated.resources.placeholder_generic_error_retry
import snout.composeapp.generated.resources.placeholder_generic_error_subtitle
import snout.composeapp.generated.resources.placeholder_generic_error_title
import snout.composeapp.generated.resources.placeholder_nothing_found_retry
import snout.composeapp.generated.resources.placeholder_nothing_found_subtitle
import snout.composeapp.generated.resources.placeholder_nothing_found_title
import snout.composeapp.generated.resources.placeholder_payment_required_retry
import snout.composeapp.generated.resources.placeholder_payment_required_subtitle
import snout.composeapp.generated.resources.placeholder_payment_required_title
import snout.composeapp.generated.resources.placeholder_permission_denied_retry
import snout.composeapp.generated.resources.placeholder_permission_denied_subtitle
import snout.composeapp.generated.resources.placeholder_permission_denied_title
import snout.composeapp.generated.resources.placeholder_rate_limited_retry
import snout.composeapp.generated.resources.placeholder_rate_limited_subtitle
import snout.composeapp.generated.resources.placeholder_rate_limited_title
import snout.composeapp.generated.resources.placeholder_search_empty_action
import snout.composeapp.generated.resources.placeholder_search_empty_message

/** A load that failed: an icon and wording for the kind of [error], and a retry. */
@Composable
internal fun ErrorPlaceholder(error: DataError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (error) {
        DataError.Unauthorized, DataError.Forbidden -> Placeholder(
            icon = painterResource(Res.drawable.ic_lock_outlined_24dp),
            title = stringResource(Res.string.placeholder_permission_denied_title),
            message = stringResource(Res.string.placeholder_permission_denied_subtitle),
            actionLabel = stringResource(Res.string.placeholder_permission_denied_retry),
            onAction = onRetry,
            modifier = modifier,
        )

        DataError.PaymentRequired -> Placeholder(
            icon = painterResource(Res.drawable.ic_credit_card_24dp),
            title = stringResource(Res.string.placeholder_payment_required_title),
            message = stringResource(Res.string.placeholder_payment_required_subtitle),
            actionLabel = stringResource(Res.string.placeholder_payment_required_retry),
            onAction = onRetry,
            modifier = modifier,
        )

        DataError.RateLimited -> Placeholder(
            icon = painterResource(Res.drawable.ic_schedule_24dp),
            title = stringResource(Res.string.placeholder_rate_limited_title),
            message = stringResource(Res.string.placeholder_rate_limited_subtitle),
            actionLabel = stringResource(Res.string.placeholder_rate_limited_retry),
            onAction = onRetry,
            modifier = modifier,
        )

        DataError.NotFound -> NothingFoundPlaceholder(onRetry = onRetry, modifier = modifier)

        DataError.ConnectionError, DataError.UnexpectedError -> Placeholder(
            icon = painterResource(Res.drawable.ic_warning_outlined_24dp),
            title = stringResource(Res.string.placeholder_generic_error_title),
            message = stringResource(Res.string.placeholder_generic_error_subtitle),
            actionLabel = stringResource(Res.string.placeholder_generic_error_retry),
            onAction = onRetry,
            modifier = modifier,
        )
    }
}

/** A load that succeeded with nothing to show; the action refreshes. */
@Composable
internal fun NothingFoundPlaceholder(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Placeholder(
        icon = painterResource(Res.drawable.ic_info_outlined_24dp),
        title = stringResource(Res.string.placeholder_nothing_found_title),
        message = stringResource(Res.string.placeholder_nothing_found_subtitle),
        actionLabel = stringResource(Res.string.placeholder_nothing_found_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

/** Empty result of a filter query; the search field stays visible above it so the query can be edited. */
@Composable
internal fun NoSearchResultsPlaceholder(title: String, onClearSearch: () -> Unit, modifier: Modifier = Modifier) {
    Placeholder(
        icon = painterResource(Res.drawable.ic_search_24dp),
        title = title,
        message = stringResource(Res.string.placeholder_search_empty_message),
        actionLabel = stringResource(Res.string.placeholder_search_empty_action),
        onAction = onClearSearch,
        modifier = modifier,
    )
}

/**
 * A whole section the PostHog instance doesn't offer: it answers 404 for the API, which older self-hosted versions
 * do. [title] names the section; [message] says why it is missing.
 */
@Composable
internal fun UnavailablePlaceholder(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Placeholder(
        icon = painterResource(Res.drawable.ic_info_outlined_24dp),
        title = title,
        message = message,
        actionLabel = stringResource(Res.string.placeholder_generic_error_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorPlaceholderGenericPreview() = ErrorPlaceholder(error = DataError.UnexpectedError, onRetry = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorPlaceholderPermissionDeniedPreview() = ErrorPlaceholder(error = DataError.Forbidden, onRetry = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorPlaceholderPaymentRequiredPreview() = ErrorPlaceholder(error = DataError.PaymentRequired, onRetry = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorPlaceholderRateLimitedPreview() = ErrorPlaceholder(error = DataError.RateLimited, onRetry = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun NothingFoundPlaceholderPreview() = NothingFoundPlaceholder(onRetry = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun NoSearchResultsPlaceholderPreview() = NoSearchResultsPlaceholder(title = "No insights match", onClearSearch = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun UnavailablePlaceholderPreview() = UnavailablePlaceholder(
    title = "Logs unavailable",
    message = "This PostHog instance doesn't offer the Logs API yet.",
    onRetry = {},
)
