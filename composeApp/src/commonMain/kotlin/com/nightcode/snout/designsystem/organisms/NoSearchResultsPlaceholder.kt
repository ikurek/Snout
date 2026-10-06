package com.nightcode.snout.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.molecules.Placeholder
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_search_24dp
import snout.composeapp.generated.resources.placeholder_search_empty_action
import snout.composeapp.generated.resources.placeholder_search_empty_message

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

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun NoSearchResultsPlaceholderPreview() = NoSearchResultsPlaceholder(title = "No insights match", onClearSearch = {})
