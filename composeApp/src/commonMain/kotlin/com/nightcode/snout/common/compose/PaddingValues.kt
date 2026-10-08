package com.nightcode.snout.common.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.minus

/** Only the bottom of these paddings, for scrollable content that should scroll behind the navigation bar. */
internal fun PaddingValues.bottomOnly(): PaddingValues = PaddingValues(bottom = calculateBottomPadding())

/** Everything but the bottom, for the container around content that pads its own bottom with [bottomOnly]. */
internal fun PaddingValues.withoutBottom(): PaddingValues = this - bottomOnly()
