package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/** Initials on a `primaryContainer` square; the same 32dp avatar sits in the app bar, project rows and activity log. */
@Composable
internal fun AvatarBox(name: String, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(SnoutTheme.shapes.medium)
            .background(SnoutTheme.colors.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(2).uppercase(),
            style = SnoutTheme.typography.labelSmall,
            color = SnoutTheme.colors.onPrimaryContainer,
        )
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AvatarBoxPreview() = AvatarBox(name = "Production")
