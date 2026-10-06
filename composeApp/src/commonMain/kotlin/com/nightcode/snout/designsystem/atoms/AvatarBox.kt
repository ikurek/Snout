package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import kotlin.math.abs

@Composable
internal fun AvatarBox(
    name: String,
    modifier: Modifier = Modifier,
    color: Color = avatarColorForName(name),
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(SnoutTheme.shapes.small)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(2).uppercase(),
            style = SnoutTheme.monoTypography.titleMedium,
        )
    }
}

@Composable
internal fun avatarColorForName(name: String): Color {
    val colors = SnoutTheme.extendedColors.chart
    return colors[abs(name.hashCode()) % colors.size]
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AvatarBoxPreview() = AvatarBox(name = "Production")
