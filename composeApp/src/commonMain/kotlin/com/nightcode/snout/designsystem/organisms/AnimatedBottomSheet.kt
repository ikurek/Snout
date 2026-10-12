package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing

private const val ScrimAlpha = 0.32f

/**
 * Modal sheet with the Snout chrome: `surfaceContainer` ground, 16dp top corners, a 32×4dp handle in
 * `outlineVariant` with 12dp above and below, and a 32% scrim. Shows and hides itself as [isVisible] changes.
 *
 * [onDismissRequest] fires once per dismissal by the person (swipe down, scrim tap, back), never when the caller
 * hides the sheet by setting [isVisible] to false; the caller is expected to set it to false in response.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnimatedBottomSheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
    content: @Composable ColumnScope.() -> Unit,
) {
    LaunchedEffect(isVisible) {
        if (isVisible) sheetState.show() else sheetState.hide()
    }
    // Leave the composition once hidden, so a closed sheet keeps no dialog window around.
    if (!sheetState.isVisible && !isVisible) {
        return
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = BottomSheetDefaults.ExpandedShape,
        containerColor = SnoutTheme.colors.surfaceContainer,
        contentColor = SnoutTheme.colors.onSurface,
        tonalElevation = 0.dp,
        scrimColor = SnoutTheme.colors.scrim.copy(alpha = ScrimAlpha),
        dragHandle = { SheetHandle() },
        content = content,
    )
}

/** Material's handle keeps 22dp above and below it for a 48dp touch target, which leaves the title floating. */
@Composable
private fun SheetHandle() {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.medium), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 4.dp)
                .clip(SnoutTheme.shapes.extraSmall)
                .background(SnoutTheme.colors.outlineVariant),
        )
    }
}
