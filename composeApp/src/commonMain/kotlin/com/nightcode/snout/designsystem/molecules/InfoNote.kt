package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_info_outlined_24dp

@Composable
internal fun InfoNote(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter = painterResource(Res.drawable.ic_info_outlined_24dp),
    containerColor: Color = SnoutTheme.colors.surfaceContainer,
    contentColor: Color = SnoutTheme.colors.onSurfaceVariant,
    iconTint: Color = SnoutTheme.colors.primary,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SnoutTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(painter = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Text(text = text, style = SnoutTheme.typography.bodySmall)
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InfoNotePreview() = InfoNote(text = "Use a personal API key with read access to this project.")
