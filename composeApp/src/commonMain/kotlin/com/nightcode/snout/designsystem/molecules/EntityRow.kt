package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.LabelChip
import com.nightcode.snout.designsystem.atoms.LabelChipTone
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * The one list row used by every list in the app: optional leading visual, title, optional subtitle,
 * optional meta line (chips and small text), optional supporting content and an optional trailing control.
 *
 * Layout rules shared by all lists: 16dp horizontal and 12dp vertical padding, 16dp gap between slots,
 * title in `titleSmall`, secondary text in `onSurfaceVariant`.
 */
@Composable
internal fun EntityRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleStyle: TextStyle = SnoutTheme.typography.titleSmall,
    subtitleStyle: TextStyle = SnoutTheme.typography.bodySmall,
    titleMaxLines: Int = 2,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    meta: (@Composable RowScope.() -> Unit)? = null,
    supporting: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = alignment,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        leading?.invoke()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = titleStyle, maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis)
            subtitle?.let {
                Text(
                    text = it,
                    style = subtitleStyle,
                    color = SnoutTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            meta?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp),
                    content = it,
                )
            }
            supporting?.invoke(this)
        }
        trailing?.invoke()
    }
}

/** Small secondary text for the meta line of an [EntityRow]. */
@Composable
internal fun MetaText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = SnoutTheme.typography.labelSmall,
        color = SnoutTheme.colors.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun EntityRowPreview() = EntityRow(
    title = "Product overview",
    subtitle = "Key metrics for the mobile app",
    leading = { AvatarBox(name = "Product overview") },
    trailing = { LabelChip(text = "Active", tone = LabelChipTone.Success) },
    meta = {
        LabelChip(text = "mobile")
        MetaText("Viewed 2 d ago")
    },
    onClick = {},
)
