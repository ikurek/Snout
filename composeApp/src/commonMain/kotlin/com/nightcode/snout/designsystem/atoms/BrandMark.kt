package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.brand_snout_logo

private val LargeMarkThreshold = 48.dp

/** The Snout logo on a `primary` square: 36dp beside the app name on login, 64dp on the splash screen. */
@Composable
internal fun BrandMark(modifier: Modifier = Modifier, size: Dp = 36.dp) {
    val shape = if (size >= LargeMarkThreshold) SnoutTheme.shapes.large else SnoutTheme.shapes.medium
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(SnoutTheme.colors.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.brand_snout_logo),
            contentDescription = null,
            tint = SnoutTheme.colors.onPrimary,
            modifier = Modifier.fillMaxSize().padding(size / 5),
        )
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun BrandMarkPreview() = BrandMark()

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun BrandMarkLargePreview() = BrandMark(size = 64.dp)
