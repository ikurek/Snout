package com.nightcode.snout.features.errortracking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.errortracking.IssueSeverity
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun IssueSeverityTag(severity: IssueSeverity, modifier: Modifier = Modifier) {
    val tone = when (severity) {
        IssueSeverity.High, IssueSeverity.Critical -> StatusTone.Attention
        IssueSeverity.Low, IssueSeverity.Medium -> StatusTone.Neutral
    }
    Tag(text = stringResource(severity.label), tone = tone, modifier = modifier)
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun IssueSeverityTagPreview() = FlowRow(
    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    verticalArrangement = Arrangement.spacedBy(Spacing.small),
) {
    IssueSeverity.entries.forEach { IssueSeverityTag(severity = it) }
}
