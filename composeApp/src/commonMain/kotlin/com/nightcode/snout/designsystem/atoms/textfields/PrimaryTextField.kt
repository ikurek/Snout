package com.nightcode.snout.designsystem.atoms.textfields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.TextUnit
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Input and placeholder text: `bodyLarge` without its line height. On iOS an empty line with a line height measures
 * about 1.3dp taller than one with text, so the box grew while empty and shrank on the first character.
 */
private val FieldTextStyle
    @Composable get() = SnoutTheme.typography.bodyLarge.copy(lineHeight = TextUnit.Unspecified)

/** Form input with its label above the box; outlined so forms stay distinct from the tonal [SearchField]. */
@Composable
internal fun PrimaryTextField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    error: TextFieldError? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        state = state,
        modifier = modifier,
        enabled = enabled,
        label = {
            Text(text = label, style = SnoutTheme.typography.bodySmall, color = SnoutTheme.colors.onSurfaceVariant)
        },
        labelPosition = TextFieldLabelPosition.Above(),
        isError = error != null,
        lineLimits = lineLimits,
        textStyle = FieldTextStyle,
        keyboardOptions = keyboardOptions,
        shape = SnoutTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SnoutTheme.colors.surfaceContainerLowest,
            unfocusedContainerColor = SnoutTheme.colors.surfaceContainerLowest,
            errorContainerColor = SnoutTheme.colors.surfaceContainerLowest,
            focusedBorderColor = SnoutTheme.colors.primary,
            unfocusedBorderColor = SnoutTheme.colors.outline,
            errorBorderColor = SnoutTheme.colors.error,
            focusedPlaceholderColor = SnoutTheme.colors.onSurfaceVariant,
            unfocusedPlaceholderColor = SnoutTheme.colors.onSurfaceVariant,
        ),
        supportingText = error?.let {
            {
                Text(
                    text = when (it) {
                        is TextFieldError.Resource -> stringResource(it.value)
                        is TextFieldError.Text -> it.value
                    },
                    style = SnoutTheme.typography.bodySmall,
                )
            }
        },
        placeholder = placeholder?.let { { Text(it, style = FieldTextStyle) } },
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrimaryTextFieldEmptyPreview() = PrimaryTextField(
    state = rememberTextFieldState(),
    label = "Instance URL",
    placeholder = "https://eu.posthog.com",
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrimaryTextFieldFilledPreview() = PrimaryTextField(
    state = rememberTextFieldState(initialText = "https://eu.posthog.com"),
    label = "Instance URL",
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrimaryTextFieldDisabledPreview() = PrimaryTextField(
    state = rememberTextFieldState(initialText = "https://eu.posthog.com"),
    label = "Instance URL",
    enabled = false,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrimaryTextFieldErrorPreview() = PrimaryTextField(
    state = rememberTextFieldState(initialText = "eu.posthog"),
    label = "Instance URL",
    error = TextFieldError.Text("Value is not a valid HTTPS url"),
)
