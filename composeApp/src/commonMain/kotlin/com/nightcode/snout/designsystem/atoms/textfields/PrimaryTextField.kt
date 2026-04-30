package com.nightcode.snout.designsystem.atoms.textfields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_arrow_right_alt_24dp

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
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        state = state,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label) },
        isError = error != null,
        lineLimits = lineLimits,
        keyboardOptions = keyboardOptions,
        supportingText = error?.let {
            {
                Text(
                    when (it) {
                        is TextFieldError.Resource -> stringResource(it.value)
                        is TextFieldError.Text -> it.value
                    },
                )
            }
        },
        labelPosition = TextFieldLabelPosition.Above(),
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryTextFieldEmptyPreview() = PrimaryTextField(
    state = rememberTextFieldState(),
    label = "Label",
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryTextFieldEmptyDisabledPreview() = PrimaryTextField(
    state = rememberTextFieldState(),
    label = "Label",
    enabled = false,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryTextFieldEmptyWithPlaceholderPreview() = PrimaryTextField(
    state = rememberTextFieldState(),
    label = "Label",
    placeholder = "Placeholder",
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryTextFieldEmptyWithPlaceholderDisabledPreview() = PrimaryTextField(
    state = rememberTextFieldState(),
    label = "Label",
    placeholder = "Placeholder",
    enabled = false,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryTextFieldFilledPreview() = PrimaryTextField(
    state = rememberTextFieldState(initialText = "Sample text"),
    label = "Label",
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryTextFieldFilledDisabledPreview() = PrimaryTextField(
    state = rememberTextFieldState(initialText = "Sample text"),
    label = "Label",
    enabled = false,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryTextFieldFilledWithIconsPreview() = PrimaryTextField(
    state = rememberTextFieldState(initialText = "Sample text"),
    label = "Label",
    leadingIcon = {
        Icon(painterResource(Res.drawable.ic_arrow_right_alt_24dp), contentDescription = null)
    },
    trailingIcon = {
        Icon(painterResource(Res.drawable.ic_arrow_right_alt_24dp), contentDescription = null)
    },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun PrimaryTextFieldFilledErrorPreview() = PrimaryTextField(
    state = rememberTextFieldState(initialText = "Sample text"),
    label = "Label",
    error = TextFieldError.Text("Error"),
)
