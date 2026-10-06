package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.tokens.SnoutTheme

private const val ScrimAlpha = 0.32f

/**
 * Modal sheet with the Snout chrome: `surfaceContainer` ground, 16dp top corners, a 32×4dp handle in
 * `outlineVariant` and a 32% scrim. Shows and hides itself as [isVisible] changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedBottomSheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
    content: @Composable ColumnScope.() -> Unit,
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            sheetState.show()
        } else {
            sheetState.hide()
            onDismissRequest()
        }
    }
    // Make sure we dispose of the bottom sheet when it is no longer needed
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
        dragHandle = {
            BottomSheetDefaults.DragHandle(width = 32.dp, height = 4.dp, color = SnoutTheme.colors.outlineVariant)
        },
        content = content,
    )
}
