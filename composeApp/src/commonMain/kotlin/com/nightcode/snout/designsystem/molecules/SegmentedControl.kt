package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/** Single-choice range or mode selector. Selection is tonal (`secondaryContainer`), the border is `outline`. */
@Composable
internal fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = SnoutTheme.colors.secondaryContainer,
        activeContentColor = SnoutTheme.colors.onSecondaryContainer,
        activeBorderColor = SnoutTheme.colors.outline,
        inactiveContainerColor = Color.Transparent,
        inactiveContentColor = SnoutTheme.colors.onSurface,
        inactiveBorderColor = SnoutTheme.colors.outline,
    )
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = colors,
                label = { Text(label(option), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SegmentedControlPreview() = SegmentedControl(
    options = listOf("Today", "7 days", "30 days", "90 days"),
    selected = "7 days",
    onSelect = {},
    label = { it },
)
