package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_chevron_right_24dp

/**
 * Label-over-value pair for detail screens. The value is `bodyLarge`, or monospace when [valueIsCode] marks it
 * as a key someone would paste. A clickable row without its own [trailing] shows a chevron.
 */
@Composable
internal fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueIsCode: Boolean = false,
    leadingIcon: Painter? = null,
    valueMaxLines: Int = 2,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        leadingIcon?.let {
            Icon(
                painter = it,
                contentDescription = null,
                tint = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = if (valueIsCode) SnoutTheme.code.medium else SnoutTheme.typography.bodyLarge,
                    maxLines = valueMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(
                painter = painterResource(Res.drawable.ic_chevron_right_24dp),
                contentDescription = null,
                tint = SnoutTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun KeyValueRowPreview() = KeyValueRow(label = "Instance", value = "https://eu.posthog.com")

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun KeyValueRowCodePreview() = KeyValueRow(label = "Feature flag", value = "new-checkout", valueIsCode = true, onClick = {})
