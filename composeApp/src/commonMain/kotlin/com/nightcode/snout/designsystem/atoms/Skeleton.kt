package com.nightcode.snout.designsystem.atoms

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing

private const val ShimmerMinAlpha = 0.55f
private const val ShimmerDurationMillis = 1200
private const val TitleWidth = 0.6f
private const val LineWidth = 0.9f
private const val MetaWidth = 0.35f
private const val DefaultRowCount = 5
private const val ThirdLineWidth = 0.8f

/** Slow alpha pulse shared by every skeleton block on screen so they breathe together. */
@Composable
private fun shimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = ShimmerMinAlpha,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(ShimmerDurationMillis), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    return alpha
}

/** One placeholder block; size it with [modifier]. */
@Composable
internal fun SkeletonBlock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .alpha(shimmerAlpha())
            .clip(SnoutTheme.shapes.extraSmall)
            .background(SnoutTheme.colors.surfaceContainerHigh),
    )
}

/** Title, description and meta blocks with the same padding as a list row; stands in for an EntityRow while loading. */
@Composable
internal fun SkeletonRow(modifier: Modifier = Modifier, divider: Boolean = true) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            SkeletonBlock(Modifier.fillMaxWidth(TitleWidth).height(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth(LineWidth).height(12.dp))
            SkeletonBlock(Modifier.fillMaxWidth(MetaWidth).height(10.dp))
        }
        if (divider) {
            Hairline(inset = true)
        }
    }
}

/** Loading state for list and detail screens; never a centred spinner. */
@Composable
internal fun SkeletonRows(modifier: Modifier = Modifier, count: Int = DefaultRowCount) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(count) { index -> SkeletonRow(divider = index < count - 1) }
    }
}

/** Three text-like lines, used inside cards whose content is still computing. */
@Composable
internal fun SkeletonLines(modifier: Modifier = Modifier, lineHeight: Dp = 12.dp) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        SkeletonBlock(Modifier.fillMaxWidth(LineWidth).height(lineHeight))
        SkeletonBlock(Modifier.fillMaxWidth(TitleWidth).height(lineHeight))
        SkeletonBlock(Modifier.fillMaxWidth(ThirdLineWidth).height(lineHeight))
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SkeletonRowsPreview() = SkeletonRows(count = 3)
