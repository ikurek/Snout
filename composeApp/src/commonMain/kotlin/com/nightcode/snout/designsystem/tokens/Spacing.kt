package com.nightcode.snout.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * Spacing scale for padding, gaps and spacers: a 4dp grid plus 2dp and 6dp for tight inline gaps. Screen edges and
 * card padding are [large]. Component sizes (icons, heights, widths, radii, strokes) stay plain `dp`.
 *
 * A plain object rather than a [SnoutTheme] value: spacing is the same in light and dark, so there is nothing to
 * provide through composition, and the constants read the same inside and outside composable code.
 */
object Spacing {
    val xxxSmall = 2.dp
    val xxSmall = 4.dp
    val xSmall = 6.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val xLarge = 20.dp
    val xxLarge = 24.dp
    val xxxLarge = 32.dp
    val huge = 48.dp
}
