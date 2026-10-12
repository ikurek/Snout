package com.nightcode.snout.designsystem.atoms.chips

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * Compact single-choice chip (chart metric, filter); selection is tonal like everywhere else in Snout.
 * [trailingIcon] is for a dropdown arrow when the chip opens a menu.
 */
@Composable
internal fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, style = SnoutTheme.typography.labelMedium) },
        modifier = modifier,
        enabled = enabled,
        trailingIcon = trailingIcon,
        shape = SnoutTheme.shapes.extraSmall,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SnoutTheme.colors.secondaryContainer,
            selectedLabelColor = SnoutTheme.colors.onSecondaryContainer,
            labelColor = SnoutTheme.colors.onSurfaceVariant,
            iconColor = SnoutTheme.colors.onSurfaceVariant,
            selectedTrailingIconColor = SnoutTheme.colors.onSecondaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = SnoutTheme.colors.outline,
            selectedBorderColor = SnoutTheme.colors.secondaryContainer,
        ),
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ChoiceChipPreview() = ChoiceChip(text = "Visitors", selected = true, onClick = {})
