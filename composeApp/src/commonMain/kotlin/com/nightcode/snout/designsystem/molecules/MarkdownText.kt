package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing

/**
 * Text PostHog stores as markdown (text tiles, insight and dashboard descriptions), rendered in [style] and the
 * current content colour. Headings step down from `titleMedium` so a heading in a tile never outweighs the screen
 * title; links are `primary`, code uses the monospace styles on `surfaceContainerHigh`.
 *
 * Parsed while composing rather than in the background: these texts are short, and parsing late would make a tile
 * grow after it scrolled in.
 */
@Composable
internal fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SnoutTheme.typography.bodyMedium,
    color: Color = LocalContentColor.current,
) {
    val text = style.copy(color = color)
    val heading = SnoutTheme.typography.titleMedium.copy(color = color)
    Markdown(
        markdownState = rememberMarkdownState(markdown, immediate = true),
        colors = markdownColor(
            text = color,
            codeBackground = SnoutTheme.colors.surfaceContainerHigh,
            dividerColor = SnoutTheme.colors.outlineVariant,
            tableBackground = Color.Transparent,
        ),
        typography = markdownTypography(
            h1 = heading,
            h2 = heading,
            h3 = SnoutTheme.typography.titleSmall.copy(color = color),
            h4 = text.copy(fontWeight = FontWeight.SemiBold),
            h5 = text.copy(fontWeight = FontWeight.SemiBold),
            h6 = text.copy(fontWeight = FontWeight.SemiBold),
            text = text,
            code = SnoutTheme.code.small.copy(color = color),
            // Monospace at the surrounding text's size, so inline code doesn't change the line height.
            inlineCode = text.merge(
                SnoutTheme.code.medium.copy(fontSize = style.fontSize, lineHeight = style.lineHeight),
            ),
            quote = text.copy(color = SnoutTheme.colors.onSurfaceVariant),
            paragraph = text,
            ordered = text,
            bullet = text,
            list = text,
            textLink = TextLinkStyles(
                style = SpanStyle(color = SnoutTheme.colors.primary, textDecoration = TextDecoration.Underline),
            ),
            table = text,
        ),
        modifier = modifier,
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun MarkdownTextPreview() = Surface {
    MarkdownText(
        markdown = """
            ## Weekly goals
            Grow **signups** by 10% and keep *churn* flat. See the [launch plan](https://posthog.com).

            - Ship `onboarding_v2`
            - Fix the pricing page funnel
        """.trimIndent(),
        modifier = Modifier.fillMaxWidth().padding(Spacing.large),
    )
}
