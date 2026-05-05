package com.nightcode.snout.designsystem.atoms.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_arrow_left_alt_24dp
import snout.composeapp.generated.resources.navigate_back_content_description

@Composable
internal fun NavigationBackIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_left_alt_24dp),
            contentDescription = stringResource(Res.string.navigate_back_content_description),
        )
    }
}
