package com.nightcode.snout.features.featureflags

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.textfields.SearchField
import com.nightcode.snout.designsystem.molecules.CodeChip
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.organisms.NoSearchResultsPlaceholder
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.featureflags.components.FeatureFlagStatusPill
import com.nightcode.snout.features.featureflags.components.rolloutSummary
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flags_search_empty_title
import snout.composeapp.generated.resources.feature_flags_search_placeholder

@Composable
internal fun FeatureFlagsScreen(
    state: FeatureFlagsUiState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenFlag: (flagId: Long) -> Unit,
) {
    val searchState = rememberTextFieldState(state.query)
    LaunchedEffect(searchState.text) { onQueryChange(searchState.text.toString()) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(
            state = searchState,
            placeholder = stringResource(Res.string.feature_flags_search_placeholder),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        CachedStateContainer(
            state = state.flags,
            onRefresh = onRefresh,
            emptyContent = {
                if (state.query.isBlank()) {
                    NothingFoundPlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
                } else {
                    NoSearchResultsPlaceholder(
                        title = stringResource(Res.string.feature_flags_search_empty_title),
                        onClearSearch = { searchState.clearText() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            },
        ) { flags ->
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                itemsIndexed(flags, key = { _, flag -> flag.id }) { index, flag ->
                    FeatureFlagRow(flag = flag, divider = index < flags.lastIndex, onClick = { onOpenFlag(flag.id) })
                }
            }
        }
    }
}

/** Key in code style, description, then either the rollout summary or a remote configuration's value as a chip. */
@Composable
private fun FeatureFlagRow(flag: FeatureFlag, divider: Boolean, onClick: () -> Unit) {
    val remoteConfigValue = flag.booleanPayload?.takeIf { flag.isRemoteConfiguration }
    EntityRow(
        title = flag.key,
        titleStyle = SnoutTheme.code.medium,
        titleMaxLines = 1,
        subtitle = flag.description,
        divider = divider,
        onClick = onClick,
        meta = {
            if (remoteConfigValue != null) {
                CodeChip(text = remoteConfigValue.singleLine(), modifier = Modifier.weight(1f, fill = false))
            } else {
                MetaText(flag.rolloutSummary())
            }
        },
        trailing = { FeatureFlagStatusPill(status = flag.status) },
    )
}

/** Collapses a pretty-printed payload into one line for list previews. */
private fun String.singleLine(): String = lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagsScreenPreview() = FeatureFlagsScreen(
    state = TestFeatureFlagsUiState,
    onQueryChange = {},
    onRefresh = {},
    onOpenFlag = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagsScreenEmptyPreview() = FeatureFlagsScreen(
    state = FeatureFlagsUiState(flags = CachedUiState.Empty(isRefreshing = false)),
    onQueryChange = {},
    onRefresh = {},
    onOpenFlag = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagsScreenForbiddenPreview() = FeatureFlagsScreen(
    state = FeatureFlagsUiState(flags = CachedUiState.Error(DataError.Forbidden)),
    onQueryChange = {},
    onRefresh = {},
    onOpenFlag = {},
)
