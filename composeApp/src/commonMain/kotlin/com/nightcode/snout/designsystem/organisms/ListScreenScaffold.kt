package com.nightcode.snout.designsystem.organisms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton

/** Top-level screen chrome used by the secondary sections: title, back, open in PostHog, optional FAB. */
@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { NavigationBackIconButton(onClick = onNavigateBack) },
                actions = { OpenInPostHogIconButton(onClick = onOpenInPostHog) },
            )
        },
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}
