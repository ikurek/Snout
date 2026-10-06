package com.nightcode.snout.designsystem.atoms.textfields

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.clear_search_content_description
import snout.composeapp.generated.resources.ic_close_24dp
import snout.composeapp.generated.resources.ic_search_24dp

/** Tonal, fully rounded filter field at the top of Insights and Flags; the fill is the affordance, no border. */
@Composable
internal fun SearchField(state: TextFieldState, placeholder: String, modifier: Modifier = Modifier) {
    TextField(
        state = state,
        modifier = modifier,
        placeholder = { Text(placeholder, style = SnoutTheme.typography.bodyLarge) },
        lineLimits = TextFieldLineLimits.SingleLine,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SnoutTheme.colors.surfaceContainer,
            unfocusedContainerColor = SnoutTheme.colors.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = SnoutTheme.colors.onSurfaceVariant,
            unfocusedLeadingIconColor = SnoutTheme.colors.onSurfaceVariant,
            focusedTrailingIconColor = SnoutTheme.colors.onSurfaceVariant,
            unfocusedTrailingIconColor = SnoutTheme.colors.onSurfaceVariant,
            focusedPlaceholderColor = SnoutTheme.colors.onSurfaceVariant,
            unfocusedPlaceholderColor = SnoutTheme.colors.onSurfaceVariant,
        ),
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.ic_search_24dp),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = if (state.text.isEmpty()) {
            null
        } else {
            {
                IconButton(onClick = { state.clearText() }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close_24dp),
                        contentDescription = stringResource(Res.string.clear_search_content_description),
                        modifier = Modifier.size(20.dp),
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

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SearchFieldEmptyPreview() = SearchField(state = rememberTextFieldState(), placeholder = "Search flags by key")
