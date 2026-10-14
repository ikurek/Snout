package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton

/**
 * Chrome for screens pushed over the home shell (details, settings pages, sections opened from More): a
 * [SnoutTopAppBar] with a back arrow and [title], then [actions] and, when [onOpenInPostHog] is given, the open in
 * PostHog button last. The content gets the system bar padding to apply itself.
 */
@Composable
internal fun PushedScreenScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenInPostHog: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SnoutTopAppBar(
                title = title,
                navigationIcon = { NavigationBackIconButton(onClick = onNavigateBack) },
                actions = {
                    actions()
                    onOpenInPostHog?.let { OpenInPostHogIconButton(onClick = it) }
                },
            )
        },
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        content = content,
    )
}
