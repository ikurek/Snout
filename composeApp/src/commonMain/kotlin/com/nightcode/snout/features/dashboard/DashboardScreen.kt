package com.nightcode.snout.features.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.dashboard_screen_title

@Composable
internal fun DashboardScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.dashboard_screen_title),
            style = SnoutTheme.typography.titleMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
        )
    }
}
