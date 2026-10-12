package com.nightcode.snout.features.logs.components

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
import com.nightcode.snout.shared.domain.model.logs.LogSeverity

/** Upper-case level the way log viewers print it; only warnings and errors take a colour. */
@Composable
internal fun LogSeverityTag(severity: LogSeverity, modifier: Modifier = Modifier) {
    val tone = when (severity) {
        LogSeverity.Error, LogSeverity.Fatal -> StatusTone.Attention
        LogSeverity.Warn -> StatusTone.Info
        LogSeverity.Trace, LogSeverity.Debug, LogSeverity.Info, LogSeverity.Unknown -> StatusTone.Neutral
    }
    val text = if (severity == LogSeverity.Unknown) "?" else severity.name.uppercase()
    Tag(text = text, tone = tone, modifier = modifier)
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LogSeverityTagPreview() = FlowRow(
    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    verticalArrangement = Arrangement.spacedBy(Spacing.small),
) {
    LogSeverity.entries.forEach { LogSeverityTag(severity = it) }
}
