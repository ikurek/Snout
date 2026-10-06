package com.nightcode.snout.features.activitylog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.organisms.ListScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
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
import snout.composeapp.generated.resources.more_activity_log_title

private const val CollapsedChanges = 2

@Composable
internal fun ActivityLogScreen(
    state: CachedUiState<List<ActivityEntry>>,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    ListScreenScaffold(
        title = stringResource(Res.string.more_activity_log_title),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        CachedStateContainer(
            state = state,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(it, key = { entry -> entry.id }) { entry -> ActivityRow(entry) }
            }
        }
    }
}

@Composable
private fun ActivityRow(entry: ActivityEntry) {
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }
    val actor = entry.actor ?: stringResource(Res.string.activity_system_actor)
    val expandable = entry.changes.size > CollapsedChanges
    EntityRow(
        title = buildString {
            append(actor)
            append(' ')
            append(entry.activity.replace('_', ' '))
            append(' ')
            append(entry.scope.humanizeScope())
            entry.itemName?.let { append(" · ").append(it) }
        },
        titleStyle = SnoutTheme.typography.bodyMedium,
        titleMaxLines = 3,
        alignment = Alignment.Top,
        onClick = if (expandable) ({ expanded = expanded.not() }) else null,
        leading = { AvatarBox(name = actor, size = 36.dp) },
        meta = { MetaText(entry.createdAt.formatRelative()) },
        supporting = {
            val visible = if (expanded) entry.changes else entry.changes.take(CollapsedChanges)
            visible.forEach { ChangeLine(it) }
            AnimatedVisibility(visible = expanded.not() && expandable) {
                Text(
                    text = stringResource(Res.string.activity_more_changes, entry.changes.size - CollapsedChanges),
                    style = SnoutTheme.typography.labelSmall,
                    color = SnoutTheme.colors.primary,
                )
            }
        },
    )
}

@Composable
private fun ChangeLine(change: ActivityChange) {
    val empty = stringResource(Res.string.activity_change_empty_value)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = change.field.replace('_', ' '),
            style = SnoutTheme.monoTypography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "${change.before ?: empty} → ${change.after ?: empty}",
            style = SnoutTheme.monoTypography.labelSmall,
            color = SnoutTheme.colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** "FeatureFlag" -> "feature flag" */
private fun String.humanizeScope(): String = replace(Regex("(?<=[a-z])(?=[A-Z])"), " ").lowercase()

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ActivityLogScreenPreview() = ActivityLogScreen(
    state = CachedUiState.Content(
        listOf(
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
        isRefreshing = false,
    ),
    onNavigateBack = {},
    onRefresh = {},
    onOpenInPostHog = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ActivityLogScreenPaymentRequiredPreview() = ActivityLogScreen(
    state = CachedUiState.Error(DataError.PaymentRequired),
    onNavigateBack = {},
    onRefresh = {},
    onOpenInPostHog = {},
)
