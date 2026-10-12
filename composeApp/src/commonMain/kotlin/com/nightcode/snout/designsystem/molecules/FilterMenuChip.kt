package com.nightcode.snout.designsystem.molecules

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.chips.ChoiceChip
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_arrow_drop_down_24dp
import snout.composeapp.generated.resources.ic_check_24dp

private const val ExpandedArrowRotation = 180f

/** One choice in a [FilterMenuChip]; [supportingText] sits under the label in the menu. */
internal data class MenuOption<T>(val value: T, val label: String, val supportingText: String? = null)

/**
 * A filter chip with a dropdown arrow that opens a Material menu of [options], the current one checked.
 * [label] names the chip's current value. [active] draws the chip selected (tonal) to show the filter narrows the
 * list; leave it false while the filter is at its "everything" value.
 */
// The grouped menu is stable on Android; JetBrains' iOS build of the same Material3 still marks it expressive.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun <T> FilterMenuChip(
    label: String,
    options: List<MenuOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (expanded) ExpandedArrowRotation else 0f)
    Box(modifier = modifier) {
        ChoiceChip(
            text = label,
            selected = active,
            onClick = { expanded = true },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_drop_down_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize).rotate(arrowRotation),
                )
            },
        )
        DropdownMenuPopup(expanded = expanded, onDismissRequest = { expanded = false }) {
            FilterMenuOptions(
                options = options,
                selected = selected,
                onSelect = {
                    onSelect(it)
                    expanded = false
                },
            )
        }
    }
}

/** The menu's single group of checkable options; split out so previews can show it without a popup. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <T> FilterMenuOptions(options: List<MenuOption<T>>, selected: T, onSelect: (T) -> Unit) {
    // Material's menus mark the checked item with tertiary, which is Ember's info blue; Snout selects with secondary.
    val colors = MenuDefaults.selectableItemColors(
        selectedContainerColor = SnoutTheme.colors.secondaryContainer,
        selectedTextColor = SnoutTheme.colors.onSecondaryContainer,
        selectedLeadingIconColor = SnoutTheme.colors.onSecondaryContainer,
        selectedTrailingIconColor = SnoutTheme.colors.onSecondaryContainer,
    )
    DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 0, count = 1)) {
        options.forEachIndexed { index, option ->
            DropdownMenuItem(
                checked = option.value == selected,
                onCheckedChange = { onSelect(option.value) },
                text = { Text(option.label) },
                shapes = MenuDefaults.itemShape(index, options.size),
                checkedLeadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check_24dp),
                        contentDescription = null,
                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                    )
                },
                supportingText = option.supportingText?.let { { Text(it) } },
                colors = colors,
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FilterMenuChipPreview() = Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
    FilterMenuChip(
        label = "Active",
        options = listOf(MenuOption("active", "Active"), MenuOption("resolved", "Resolved")),
        selected = "active",
        onSelect = {},
    )
    FilterMenuChip(
        label = "All versions",
        options = listOf(MenuOption<String?>(null, "All versions")),
        selected = null,
        onSelect = {},
        active = false,
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FilterMenuOptionsPreview() = FilterMenuOptions(
    options = listOf(
        MenuOption<String?>(null, "All versions"),
        MenuOption("2.14.0", "2.14.0", supportingText = "1.1k exceptions this week"),
        MenuOption("2.13.2", "2.13.2", supportingText = "186 exceptions this week"),
    ),
    selected = "2.14.0",
    onSelect = {},
)
