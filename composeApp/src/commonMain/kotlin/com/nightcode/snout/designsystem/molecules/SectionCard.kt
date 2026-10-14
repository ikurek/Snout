package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.TrendChip
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing

/**
 * Chart card and dashboard tile: `surfaceContainerLow` on 16dp radius with a title row and 16dp padding.
 * Clickable when [onClick] is given. Tiers, not borders: no outline, no shadow.
 */
@Composable
internal fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = SnoutTheme.shapes.large
    val color = SnoutTheme.colors.surfaceContainerLow
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, color = color) {
            SectionCardContent(title, subtitle, trailing, content)
        }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = shape, color = color) {
            SectionCardContent(title, subtitle, trailing, content)
        }
    }
}

@Composable
private fun SectionCardContent(
    title: String,
    subtitle: String?,
    trailing: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.padding(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SnoutTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = SnoutTheme.typography.bodySmall,
                        color = SnoutTheme.colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke()
        }
        content()
    }
}

/**
 * Smaller tonal card: `surfaceContainerLow` on 12dp radius, without a title row. Groups a few related rows or
 * controls inside a screen; [contentPadding] and [verticalArrangement] default to 12dp padding and 4dp gaps.
 */
@Composable
internal fun TonalCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Spacing.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SnoutTheme.shapes.medium,
        color = SnoutTheme.colors.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
            content = content,
        )
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SectionCardPreview() = SectionCard(
    title = "Weekly active users",
    subtitle = "Last 8 weeks",
    trailing = { TrendChip(changePercent = 12.8) },
    modifier = Modifier.padding(Spacing.large),
) {
    Text(text = "4,812", style = SnoutTheme.typography.headlineMedium)
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun TonalCardPreview() = TonalCard(modifier = Modifier.padding(Spacing.large)) {
    Text(text = "Release condition 1", style = SnoutTheme.typography.titleSmall)
    Text(
        text = "50% of users where country is Poland",
        style = SnoutTheme.typography.bodyMedium,
        color = SnoutTheme.colors.onSurfaceVariant,
    )
}
