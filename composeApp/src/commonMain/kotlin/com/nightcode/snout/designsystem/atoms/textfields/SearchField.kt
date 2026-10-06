package com.nightcode.snout.designsystem.atoms.textfields

import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.clear_search_content_description
import snout.composeapp.generated.resources.ic_close_24dp
import snout.composeapp.generated.resources.ic_search_24dp

@Composable
internal fun SearchField(state: TextFieldState, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        state = state,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        lineLimits = TextFieldLineLimits.SingleLine,
        shape = SnoutTheme.shapes.large,
        leadingIcon = { Icon(painterResource(Res.drawable.ic_search_24dp), contentDescription = null) },
        trailingIcon = if (state.text.isEmpty()) {
            null
        } else {
            {
                IconButton(onClick = { state.clearText() }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close_24dp),
                        contentDescription = stringResource(Res.string.clear_search_content_description),
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SearchFieldPreview() = SearchField(state = rememberTextFieldState("daily"), placeholder = "Search insights")
