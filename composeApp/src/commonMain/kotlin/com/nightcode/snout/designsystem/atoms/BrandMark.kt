package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.brand_snout_logo

private val LargeMarkThreshold = 48.dp

/**
 * The app icon itself, in its own colours in both themes so it matches the launcher; marks from 48dp get larger
 * corners.
 */
@Composable
internal fun BrandMark(modifier: Modifier = Modifier, size: Dp = 36.dp) {
    val shape = if (size >= LargeMarkThreshold) SnoutTheme.shapes.large else SnoutTheme.shapes.medium
    Image(
        painter = painterResource(Res.drawable.brand_snout_logo),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(shape),
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun BrandMarkPreview() = BrandMark()

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun BrandMarkLargePreview() = BrandMark(size = 64.dp)
