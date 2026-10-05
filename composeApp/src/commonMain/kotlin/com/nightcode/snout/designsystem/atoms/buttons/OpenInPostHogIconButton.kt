package com.nightcode.snout.designsystem.atoms.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_open_in_new_24dp
import snout.composeapp.generated.resources.open_in_posthog_content_description

@Composable
internal fun OpenInPostHogIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.ic_open_in_new_24dp),
            contentDescription = stringResource(Res.string.open_in_posthog_content_description),
        )
    }
}
