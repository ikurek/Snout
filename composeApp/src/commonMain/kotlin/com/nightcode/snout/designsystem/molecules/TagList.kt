package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing

/** Labelled wrap of tags, laid out like a [KeyValueRow] so it lines up in detail screens. */
@Composable
internal fun TagList(label: String, tags: List<String>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.large, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.xSmall),
    ) {
        Text(text = label, style = SnoutTheme.typography.bodySmall, color = SnoutTheme.colors.onSurfaceVariant)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall),
            verticalArrangement = Arrangement.spacedBy(Spacing.xSmall),
        ) {
            tags.forEach { Tag(text = it) }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun TagListPreview() = TagList(label = "Tags", tags = listOf("growth", "mobile", "weekly"))
