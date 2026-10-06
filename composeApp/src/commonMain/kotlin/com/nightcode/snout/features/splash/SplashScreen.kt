package com.nightcode.snout.features.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.BrandMark
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.app_name
import snout.composeapp.generated.resources.login_screen_app_name_subtitle

/** Brand mark, name and tagline centred on the background while the initial location resolves. No spinner. */
@Composable
internal fun SplashScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandMark(size = 64.dp)
            VerticalSpacer(16.dp)
            Text(text = stringResource(Res.string.app_name), style = SnoutTheme.typography.headlineSmall)
            VerticalSpacer(4.dp)
            Text(
                text = stringResource(Res.string.login_screen_app_name_subtitle),
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SplashScreenPreview() = SplashScreen()
