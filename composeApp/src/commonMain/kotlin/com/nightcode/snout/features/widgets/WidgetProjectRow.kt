package com.nightcode.snout.features.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.FilterMenuOptions
import com.nightcode.snout.designsystem.molecules.MenuOption
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.widgets.ProjectChoice
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_arrow_drop_down_24dp
import snout.composeapp.generated.resources.widget_project_choose
import snout.composeapp.generated.resources.widget_project_failed
import snout.composeapp.generated.resources.widget_project_loading
import snout.composeapp.generated.resources.widget_project_none
import snout.composeapp.generated.resources.widget_project_title

private const val ExpandedArrowRotation = 180f

/**
 * The project a widget shows, opening a menu of every project to pick it; [projects] is null while they load, or when
 * they failed to ([failed]), and then a tap tries again ([onRetry]). Nothing is picked for a new widget: a widget's
 * project is its own, and picking one here doesn't change the app's.
 */
@Composable
internal fun WidgetProjectRow(
    projects: List<ProjectChoice>?,
    failed: Boolean,
    projectId: Long?,
    onProjectSelected: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val project = projects?.firstOrNull { it.id == projectId }
    val placeholder = when {
        projects == null && failed -> Res.string.widget_project_failed
        projects == null -> Res.string.widget_project_loading
        projects.isEmpty() -> Res.string.widget_project_none
        else -> Res.string.widget_project_choose
    }
    WidgetDropdownRow(
        label = stringResource(Res.string.widget_project_title),
        value = project?.name ?: stringResource(placeholder),
        subtitle = project?.organization,
        options = projects?.map { MenuOption(it.id, it.name, it.organization) },
        selected = projectId,
        onSelect = onProjectSelected,
        modifier = modifier,
        onClickWithoutOptions = if (projects == null && failed) onRetry else null,
    )
}

/**
 * One of a widget's settings as a dropdown: the row shows [value], and a tap opens a menu of [options] under it, the
 * current one checked, as the app's filter chips do. Without options (null while they load, or none) a tap does
 * [onClickWithoutOptions], or nothing. [label] names the setting for accessibility.
 */
// The grouped menu is stable on Android; JetBrains' iOS build of the same Material3 still marks it expressive.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun <T> WidgetDropdownRow(
    label: String,
    value: String,
    options: List<MenuOption<T>>?,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClickWithoutOptions: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (expanded) ExpandedArrowRotation else 0f)
    Box(modifier = modifier) {
        EntityRow(
            title = value,
            subtitle = subtitle,
            titleStyle = SnoutTheme.typography.bodyLarge,
            divider = false,
            onClick = if (options.isNullOrEmpty()) onClickWithoutOptions else ({ expanded = true }),
            trailing = {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_drop_down_24dp),
                    contentDescription = label,
                    tint = SnoutTheme.colors.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotation),
                )
            },
        )
        DropdownMenuPopup(expanded = expanded, onDismissRequest = { expanded = false }) {
            FilterMenuOptions(
                options = options.orEmpty(),
                selected = selected,
                onSelect = {
                    expanded = false
                    onSelect(it)
                },
            )
        }
    }
}
