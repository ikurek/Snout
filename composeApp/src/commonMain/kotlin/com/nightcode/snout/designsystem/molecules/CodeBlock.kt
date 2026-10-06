package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/** Read-only, selectable monospace block for JSON payloads and similar machine values. */
@Composable
internal fun CodeBlock(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SnoutTheme.shapes.medium,
        color = SnoutTheme.colors.surfaceContainer,
    ) {
        SelectionContainer {
            Text(text = text, style = SnoutTheme.code.medium, modifier = Modifier.padding(16.dp))
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun CodeBlockPreview() = CodeBlock(text = "{\n  \"free_views\": 3,\n  \"offering\": \"lifetime_2026\"\n}")
