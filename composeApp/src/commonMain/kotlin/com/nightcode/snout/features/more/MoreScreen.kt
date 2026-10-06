package com.nightcode.snout.features.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_chevron_right_24dp
import snout.composeapp.generated.resources.ic_history_24dp
import snout.composeapp.generated.resources.ic_science_24dp
import snout.composeapp.generated.resources.ic_sticky_note_24dp
import snout.composeapp.generated.resources.more_activity_log_subtitle
import snout.composeapp.generated.resources.more_activity_log_title
import snout.composeapp.generated.resources.more_annotations_subtitle
import snout.composeapp.generated.resources.more_annotations_title
import snout.composeapp.generated.resources.more_experiments_subtitle
import snout.composeapp.generated.resources.more_experiments_title

internal enum class MoreItem(val title: StringResource, val subtitle: StringResource, val icon: DrawableResource) {
    Experiments(Res.string.more_experiments_title, Res.string.more_experiments_subtitle, Res.drawable.ic_science_24dp),
    Annotations(
        Res.string.more_annotations_title,
        Res.string.more_annotations_subtitle,
        Res.drawable.ic_sticky_note_24dp,
    ),
    ActivityLog(
        Res.string.more_activity_log_title,
        Res.string.more_activity_log_subtitle,
        Res.drawable.ic_history_24dp,
    ),
}

@Composable
internal fun MoreScreen(onItemClick: (MoreItem) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MoreItem.entries.forEach { item ->
            KeyValueRow(
                label = stringResource(item.subtitle),
                value = stringResource(item.title),
                leadingIcon = painterResource(item.icon),
                onClick = { onItemClick(item) },
                trailing = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right_24dp),
                        contentDescription = null,
                        tint = SnoutTheme.colors.onSurfaceVariant,
                    )
                },
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun MoreScreenPreview() = MoreScreen(onItemClick = {})
