package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import com.nightcode.snout.designsystem.atoms.StatusPill
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * The one list row used by every list in the app: optional leading visual, title, optional description,
 * optional meta line (tags and small text), optional supporting content and one trailing control.
 *
 * Rows sit directly on the background with 16dp sides and 12dp vertical padding, separated by an
 * `outlineVariant` hairline. No card per row. Only the trailing control may carry accent or status colour.
 */
@Composable
internal fun EntityRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleStyle: TextStyle = SnoutTheme.typography.titleMedium,
    subtitleStyle: TextStyle = SnoutTheme.typography.bodyMedium,
    titleMaxLines: Int = 2,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    divider: Boolean = true,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    meta: (@Composable RowScope.() -> Unit)? = null,
    supporting: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
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
                        modifier = Modifier.padding(top = 4.dp),
                        content = it,
                    )
                }
                supporting?.invoke(this)
            }
            trailing?.invoke()
        }
        if (divider) {
            HorizontalDivider(color = SnoutTheme.colors.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

/** Small secondary text for the meta line of an [EntityRow]. */
@Composable
internal fun MetaText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = SnoutTheme.typography.bodySmall,
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
    trailing = { StatusPill(text = "Active", tone = StatusTone.Active) },
    meta = {
        Tag(text = "mobile")
        MetaText("Viewed 2 d ago")
    },
    onClick = {},
)
