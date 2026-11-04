package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * Organization logo when PostHog has one, otherwise the initials avatar; same 32dp square either way. The logo
 * fills the square over a white backing, so transparent dark marks stay visible on any background.
 */
@Composable
internal fun OrganizationLogo(name: String, logoUrl: String?, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    // Decoded at the size it is drawn at: the painter isn't drawn until it loads, so Coil can't measure it, and would
    // otherwise decode the logo at full resolution for a 32dp square.
    val context = LocalPlatformContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val request = remember(context, logoUrl, sizePx) {
        ImageRequest.Builder(context).data(logoUrl).size(sizePx).build()
    }
    val painter = rememberAsyncImagePainter(model = request)
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
