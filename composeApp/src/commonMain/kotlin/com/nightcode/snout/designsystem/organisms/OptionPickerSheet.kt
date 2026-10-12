package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_check_24dp

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
    modifier: Modifier = Modifier,
    leading: (@Composable (T) -> Unit)? = null,
) {
    AnimatedBottomSheet(isVisible = isVisible, onDismissRequest = onDismiss, modifier = modifier) {
        OptionPickerContent(
            title = title,
            options = options,
            selected = selected,
            onSelect = { value ->
                onSelect(value)
                onDismiss()
            },
            leading = leading,
        )
    }
}

@Composable
private fun <T> OptionPickerContent(
    title: String,
    options: List<PickerOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    leading: (@Composable (T) -> Unit)?,
) {
    Text(
        text = title,
        style = SnoutTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = Spacing.large).padding(top = Spacing.small, bottom = Spacing.medium),
    )
    Column {
        options.forEachIndexed { index, option ->
            EntityRow(
                title = option.title,
                subtitle = option.subtitle,
                subtitleStyle = SnoutTheme.typography.bodySmall,
                divider = index < options.lastIndex,
                onClick = { onSelect(option.value) },
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
    VerticalSpacer(Spacing.large)
}

/** One option in an [OptionPickerSheet]. */
internal data class PickerOption<T>(val value: T, val title: String, val subtitle: String? = null)

/** The sheet's content on its `surfaceContainer` ground; the modal itself does not render in a static preview. */
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun OptionPickerSheetPreview() = Column(modifier = Modifier.background(SnoutTheme.colors.surfaceContainer)) {
    OptionPickerContent(
        title = "Landing section",
        options = listOf(
            PickerOption(value = 0, title = "Analytics", subtitle = "Visitors, views and sessions"),
            PickerOption(value = 1, title = "Dashboards", subtitle = "Pinned and recent dashboards"),
            PickerOption(value = 2, title = "Feature flags", subtitle = "Rollouts and their state"),
        ),
        selected = 1,
        onSelect = {},
        leading = null,
    )
}
