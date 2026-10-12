package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/** Top app bar on `surface` that does not tint on scroll, with the screen title in `headlineSmall`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SnoutTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = SnoutTheme.typography.headlineSmall,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = title, style = titleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = snoutTopAppBarColors(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun snoutTopAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = SnoutTheme.colors.surface,
    scrolledContainerColor = SnoutTheme.colors.surface,
    titleContentColor = SnoutTheme.colors.onSurface,
    navigationIconContentColor = SnoutTheme.colors.onSurfaceVariant,
    actionIconContentColor = SnoutTheme.colors.onSurfaceVariant,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SnoutTopAppBarPreview() = SnoutTopAppBar(
    title = "Product overview",
    navigationIcon = { NavigationBackIconButton(onClick = {}) },
    actions = { OpenInPostHogIconButton(onClick = {}) },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SnoutTopAppBarCodeTitlePreview() = SnoutTopAppBar(
    title = "new-checkout-flow",
    titleStyle = SnoutTheme.code.large,
    navigationIcon = { NavigationBackIconButton(onClick = {}) },
)
