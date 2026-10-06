package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.AnimatedBottomSheet
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_check_24dp

/** One option in an [OptionPickerSheet]. */
internal data class PickerOption<T>(val value: T, val title: String, val subtitle: String? = null)

/**
 * Single-choice bottom sheet: a title and one [EntityRow] per option, the current one marked with a
 * `primary` check. Picking an option reports it and closes the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> OptionPickerSheet(
    isVisible: Boolean,
    title: String,
    options: List<PickerOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    leading: (@Composable (T) -> Unit)? = null,
) {
    AnimatedBottomSheet(isVisible = isVisible, onDismissRequest = onDismiss) {
        Text(
            text = title,
            style = SnoutTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        )
        Column {
            options.forEachIndexed { index, option ->
                EntityRow(
                    title = option.title,
                    subtitle = option.subtitle,
                    subtitleStyle = SnoutTheme.typography.bodySmall,
                    divider = index < options.lastIndex,
                    onClick = {
                        onSelect(option.value)
                        onDismiss()
                    },
                    leading = leading?.let { { it(option.value) } },
                    trailing = if (option.value == selected) {
                        {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check_24dp),
                                contentDescription = null,
                                tint = SnoutTheme.colors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
        VerticalSpacer(16.dp)
    }
}
