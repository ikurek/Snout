package com.nightcode.snout.features.activitylog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.molecules.CodeChip
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.RefreshableContent
import com.nightcode.snout.designsystem.organisms.RefreshableEmptyContent
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.activitylog.ActivityChange
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.activitylog.TestActivityEntry
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.activity_change_empty_value
import snout.composeapp.generated.resources.activity_more_changes
import snout.composeapp.generated.resources.activity_system_actor

private const val CollapsedChanges = 2
private const val Arrow = "→"

/** Section body only; the home shell or [com.nightcode.snout.features.home.HomeSectionRoute] supplies the chrome. */
@Composable
internal fun ActivityLogScreen(
    state: ActivityLogUiState,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val entries = state.entries
    when {
        entries == null -> LoadingOrErrorContent(error = state.error, onRetry = onRefresh)

        entries.isEmpty() -> RefreshableEmptyContent(isRefreshing = state.isRefreshing, onRefresh = onRefresh)

        else -> RefreshableContent(
            isRefreshing = state.isRefreshing,
            refreshError = state.error,
            onRefresh = onRefresh,
        ) {
            LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large) + contentPadding) {
                itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                    ActivityRow(entry = entry, divider = index < entries.lastIndex)
                }
            }
        }
    }
}

/** "Jane Doe updated feature flag `new-checkout`", the age, then one line per change; expands past two changes. */
@Composable
private fun ActivityRow(entry: ActivityEntry, divider: Boolean) {
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }
    val actor = entry.actor ?: stringResource(Res.string.activity_system_actor)
    val expandable = entry.changes.size > CollapsedChanges
    EntityRow(
        headline = { ActivitySentence(actor = actor, entry = entry) },
        alignment = Alignment.Top,
        divider = divider,
        onClick = if (expandable) ({ expanded = expanded.not() }) else null,
        leading = { AvatarBox(name = actor) },
        meta = { MetaText(entry.createdAt.formatRelative()) },
        supporting = {
            val visible = if (expanded) entry.changes else entry.changes.take(CollapsedChanges)
            visible.forEach { ChangeLine(it, modifier = Modifier.padding(top = Spacing.xSmall)) }
            AnimatedVisibility(visible = expanded.not() && expandable) {
                Text(
                    text = stringResource(Res.string.activity_more_changes, entry.changes.size - CollapsedChanges),
                    style = SnoutTheme.typography.bodySmall,
                    color = SnoutTheme.colors.primary,
                    modifier = Modifier.padding(top = Spacing.xSmall),
                )
            }
        },
    )
}

@Composable
private fun ActivitySentence(actor: String, entry: ActivityEntry) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(actor) }
                append(' ')
                append(entry.activity.replace('_', ' '))
                append(' ')
                append(entry.scope.humanizeScope())
            },
            style = SnoutTheme.typography.bodyMedium,
        )
        entry.itemName?.let { CodeChip(text = it) }
    }
}

@Composable
private fun ChangeLine(change: ActivityChange, modifier: Modifier = Modifier) {
    val empty = stringResource(Res.string.activity_change_empty_value)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        CodeChip(text = change.field.replace('_', ' '))
        CodeChip(text = change.before ?: empty)
        Text(text = Arrow, style = SnoutTheme.typography.bodySmall, color = SnoutTheme.colors.onSurfaceVariant)
        CodeChip(text = change.after ?: empty)
    }
}

/** "FeatureFlag" -> "feature flag" */
private fun String.humanizeScope(): String = replace(Regex("(?<=[a-z])(?=[A-Z])"), " ").lowercase()

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ActivityLogScreenPreview() = ActivityLogScreen(
    state = ActivityLogUiState(
        entries = listOf(
            TestActivityEntry,
            TestActivityEntry.copy(
                id = "a2",
                actor = null,
                isSystem = true,
                activity = "created",
                scope = "Insight",
                itemName = "DAU",
                changes = emptyList(),
            ),
        ),
    ),
    onRefresh = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ActivityLogScreenPaymentRequiredPreview() = ActivityLogScreen(
    state = ActivityLogUiState(error = DataError.PaymentRequired),
    onRefresh = {},
)
