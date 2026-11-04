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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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

/** The pulse of the skeleton group around a block, so the blocks of one placeholder breathe together. */
private val LocalShimmerAlpha = staticCompositionLocalOf<State<Float>?> { null }

/** Slow alpha pulse; read it only in the draw phase, so the pulse redraws the blocks without recomposing them. */
@Composable
private fun rememberShimmerAlpha(): State<Float> = rememberInfiniteTransition(label = "skeleton").animateFloat(
    initialValue = ShimmerMinAlpha,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(ShimmerDurationMillis), RepeatMode.Reverse),
    label = "skeletonAlpha",
)

/** Runs one pulse for every [SkeletonBlock] in [content] instead of one per block; nested groups share the outer. */
@Composable
internal fun SkeletonGroup(content: @Composable () -> Unit) {
    if (LocalShimmerAlpha.current != null) {
        content()
    } else {
        CompositionLocalProvider(LocalShimmerAlpha provides rememberShimmerAlpha(), content = content)
    }
}

/** One placeholder block; size it with [modifier]. */
@Composable
internal fun SkeletonBlock(modifier: Modifier = Modifier) {
    val shimmerAlpha = LocalShimmerAlpha.current ?: rememberShimmerAlpha()
    Box(
        modifier = modifier
            .graphicsLayer { alpha = shimmerAlpha.value }
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
    SkeletonGroup {
        Column(modifier = modifier.fillMaxWidth()) {
            repeat(count) { index -> SkeletonRow(divider = index < count - 1) }
        }
    }
}

/** Three text-like lines, used inside cards whose content is still computing. */
@Composable
internal fun SkeletonLines(modifier: Modifier = Modifier, lineHeight: Dp = 12.dp) {
    SkeletonGroup {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            SkeletonBlock(Modifier.fillMaxWidth(LineWidth).height(lineHeight))
            SkeletonBlock(Modifier.fillMaxWidth(TitleWidth).height(lineHeight))
            SkeletonBlock(Modifier.fillMaxWidth(ThirdLineWidth).height(lineHeight))
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SkeletonRowsPreview() = SkeletonRows(count = 3)
