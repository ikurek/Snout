package com.nightcode.snout.features.featureflags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.textfields.SearchField
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.featureflags.components.FeatureFlagStatusChip
import com.nightcode.snout.features.featureflags.components.rolloutSummary
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
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
        CachedStateContainer(state = state.flags, onRefresh = onRefresh) { flags ->
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(flags, key = { it.id }) { flag ->
                    FeatureFlagRow(flag = flag, onClick = { onOpenFlag(flag.id) })
                }
            }
        }
    }
}

@Composable
private fun FeatureFlagRow(flag: FeatureFlag, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = flag.key,
                style = SnoutTheme.monoTypography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            flag.description?.let {
                Text(
                    text = it,
                    style = SnoutTheme.typography.bodySmall,
                    color = SnoutTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = flag.rolloutSummary(),
                style = SnoutTheme.typography.labelSmall,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        }
        FeatureFlagStatusChip(status = flag.status)
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun FeatureFlagsScreenPreview() = FeatureFlagsScreen(
    state = TestFeatureFlagsUiState,
    onQueryChange = {},
    onRefresh = {},
    onOpenFlag = {},
)
