package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * Organization logo when PostHog has one, otherwise the initials avatar; same 32dp square either way. The logo
 * fills the square over a white backing, so transparent dark marks stay visible on any background.
 */
@Composable
internal fun OrganizationLogo(name: String, logoUrl: String?, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val painter = rememberAsyncImagePainter(model = logoUrl)
    val state by painter.state.collectAsStateWithLifecycle()

    if (state is AsyncImagePainter.State.Success) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(SnoutTheme.shapes.medium)
                .background(Color.White)
                .border(1.dp, SnoutTheme.colors.outlineVariant, SnoutTheme.shapes.medium),
        )
    } else {
        AvatarBox(name = name, modifier = modifier, size = size)
    }
}
