package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * Verbatim JSON or text the user may copy. One colour, no highlighting. Scrolls sideways so indentation survives;
 * set [wrap] for prose-like text such as log messages, where hidden line ends matter more than alignment.
 */
@Composable
internal fun CodeBlock(text: String, modifier: Modifier = Modifier, wrap: Boolean = false) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SnoutTheme.shapes.medium,
        color = SnoutTheme.colors.surfaceContainerHigh,
    ) {
        SelectionContainer {
            Text(
                text = text,
                style = SnoutTheme.code.medium,
                softWrap = wrap,
                modifier = Modifier
                    .then(if (wrap) Modifier else Modifier.horizontalScroll(rememberScrollState()))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

/** One inline token: a property key, a before or after value, a flag's value on its list row. */
@Composable
internal fun CodeChip(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = SnoutTheme.shapes.extraSmall, color = SnoutTheme.colors.surfaceContainerHigh) {
        Text(
            text = text,
            style = SnoutTheme.code.small,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
        )
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun CodeBlockPreview() = CodeBlock(text = "{\n  \"free_views\": 3,\n  \"offering\": \"lifetime_2026\"\n}")

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun CodeChipPreview() = CodeChip(text = "@nightcode.dev")
