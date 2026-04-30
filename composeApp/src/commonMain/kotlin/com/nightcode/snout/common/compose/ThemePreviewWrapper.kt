package com.nightcode.snout.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import com.nightcode.snout.designsystem.tokens.SnoutTheme

class ThemePreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        SnoutTheme(content = content)
    }
}
