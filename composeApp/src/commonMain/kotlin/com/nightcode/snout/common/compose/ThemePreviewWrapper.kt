package com.nightcode.snout.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

class ThemePreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        ScreenshotEnvironment(content = content)
    }
}
