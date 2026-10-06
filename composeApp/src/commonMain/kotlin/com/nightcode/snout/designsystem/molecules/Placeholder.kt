package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * Full-area state for a screen with nothing to render: icon tile, title, one sentence, one filled button.
 * Loading is never a placeholder; that is skeleton rows.
 */
@Composable
internal fun Placeholder(
    icon: Painter,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = SnoutTheme.shapes.large,
            color = SnoutTheme.colors.surfaceContainerHigh,
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.padding(16.dp).size(24.dp),
                tint = SnoutTheme.colors.onSurfaceVariant,
            )
        }
        VerticalSpacer(16.dp)
        Text(text = title, style = SnoutTheme.typography.titleMedium, textAlign = TextAlign.Center)
        VerticalSpacer(4.dp)
        Text(
            text = message,
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(20.dp)
        PrimaryButton(text = actionLabel, onClick = onAction)
    }
}
