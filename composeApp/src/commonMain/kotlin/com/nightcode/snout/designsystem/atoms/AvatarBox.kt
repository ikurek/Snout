package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

private const val TintAlpha = 0.16f
private const val HashMultiplier = 31

/**
 * Initials on a square tinted with one of the chart colours, picked from the name so the same project always
 * gets the same colour and neighbours in a list usually differ. The same 32dp avatar sits in the app bar,
 * project rows and the activity log.
 */
@Composable
internal fun AvatarBox(name: String, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val palette = SnoutTheme.extendedColors.chart
    val color = palette[name.stableHash().mod(palette.size)]
    Box(
        modifier = modifier
            .size(size)
            .clip(SnoutTheme.shapes.medium)
            .background(color.copy(alpha = TintAlpha)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(2).uppercase(),
            style = SnoutTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

/** Spelled out rather than [String.hashCode] so the colour never depends on the platform. */
private fun String.stableHash(): Int = fold(0) { hash, char -> hash * HashMultiplier + char.code }

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AvatarBoxPreview() = Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    listOf("Production", "Scandroid", "Leki w Ciąży", "Farmapedia", "Aware Beauty App").forEach {
        AvatarBox(name = it)
    }
}
