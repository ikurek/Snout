package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.tokens.SnoutTheme

@Composable
internal fun ScreenPlaceholder(
    icon: Painter,
    title: String,
    subtitle: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = SnoutTheme.shapes.extraLarge,
            color = SnoutTheme.colors.surfaceContainerHighest,
            modifier = Modifier.size(80.dp),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(20.dp)
                    .size(40.dp),
                tint = SnoutTheme.colors.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = SnoutTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = retryLabel, onClick = onRetry)
    }
}
