package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton

/** Chrome for the secondary sections and detail screens: back arrow, title, open in PostHog, optional FAB. */
@Composable
internal fun ListScreenScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    onOpenInPostHog: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            SnoutTopAppBar(
                title = title,
                navigationIcon = { NavigationBackIconButton(onClick = onNavigateBack) },
                actions = { OpenInPostHogIconButton(onClick = onOpenInPostHog) },
            )
        },
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}
