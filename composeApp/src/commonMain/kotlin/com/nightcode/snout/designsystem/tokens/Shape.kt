package com.nightcode.snout.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Three radii: 6dp for chips and tags, 12dp for cards, buttons, value boxes and banners, 16dp for chart cards,
 * sheets and dialogs. Fully rounded shapes (segmented control, pills, switches) use `CircleShape`.
 */
internal val SnoutMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp),
)
