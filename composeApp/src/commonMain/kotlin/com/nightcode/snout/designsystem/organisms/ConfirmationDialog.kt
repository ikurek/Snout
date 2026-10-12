package com.nightcode.snout.designsystem.organisms

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.dialog_cancel

/** Title as the question, one sentence, Cancel plus one confirm verb; red when [destructive]. */
@Composable
internal fun ConfirmationDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(title, style = SnoutTheme.typography.titleLarge) },
        text = { Text(text, style = SnoutTheme.typography.bodyMedium, color = SnoutTheme.colors.onSurfaceVariant) },
        shape = SnoutTheme.shapes.large,
        containerColor = SnoutTheme.colors.surfaceContainerHigh,
        titleContentColor = SnoutTheme.colors.onSurface,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (destructive) SnoutTheme.colors.error else SnoutTheme.colors.primary,
                ),
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.dialog_cancel)) }
        },
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ConfirmationDialogPreview() = ConfirmationDialog(
    title = "Log out?",
    text = "Your API key and cached data will be removed from this device.",
    confirmLabel = "Log out",
    onConfirm = {},
    onDismiss = {},
    destructive = true,
)
