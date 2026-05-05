package com.nightcode.snout.features.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.settings_screen_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_screen_title)) },
                navigationIcon = { NavigationBackIconButton(onClick = onNavigateBack) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.settings_screen_title),
                style = SnoutTheme.typography.titleMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        }
    }
}
