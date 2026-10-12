package com.nightcode.snout.features.onboarding.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.chips.ChoiceChip
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextField
import com.nightcode.snout.designsystem.atoms.textfields.TextFieldError
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.auth.InstanceUrlValidationError
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.login_screen_cloud_eu_chip
import snout.composeapp.generated.resources.login_screen_cloud_us_chip
import snout.composeapp.generated.resources.login_screen_description
import snout.composeapp.generated.resources.login_screen_header
import snout.composeapp.generated.resources.login_screen_instance_url_input_empty_error
import snout.composeapp.generated.resources.login_screen_instance_url_input_invalid_error
import snout.composeapp.generated.resources.login_screen_instance_url_input_label
import snout.composeapp.generated.resources.login_screen_instance_url_input_placeholder
import snout.composeapp.generated.resources.login_screen_personal_api_key_create_in_web_button
import snout.composeapp.generated.resources.login_screen_personal_api_key_input_empty_error
import snout.composeapp.generated.resources.login_screen_personal_api_key_input_label
import snout.composeapp.generated.resources.login_screen_personal_api_key_input_placeholder
import snout.composeapp.generated.resources.login_screen_personal_api_key_input_unable_to_connect_error

/**
 * Instance URL with Cloud US/EU shortcuts, the personal API key and a folded scope list. The fields' state lives
 * with the onboarding screen, whose fixed bottom button sends them; [bottomPadding] keeps the form above it.
 */
@Composable
internal fun LoginPage(
    instanceUrlTextFieldState: TextFieldState,
    personalApiKeyTextFieldState: TextFieldState,
    savedInstanceUrl: String?,
    instanceUrlValidationError: InstanceUrlValidationError?,
    personalApiKeyValidationError: PersonalApiKeyValidationError?,
    isConnecting: Boolean,
    bottomPadding: PaddingValues,
    onInstanceUrlChange: (instanceUrl: String) -> Unit,
    onPersonalApiKeyChange: (personalApiKey: String) -> Unit,
    onApiKeysLinkClick: (instanceUrl: String) -> Unit,
) {
    // After a 401 the instance is still known, so only the key needs typing again.
    LaunchedEffect(savedInstanceUrl) {
        if (savedInstanceUrl != null && instanceUrlTextFieldState.text.isEmpty()) {
            instanceUrlTextFieldState.setTextAndPlaceCursorAtEnd(savedInstanceUrl)
        }
    }

    LaunchedEffect(instanceUrlTextFieldState.text) {
        onInstanceUrlChange(instanceUrlTextFieldState.text.toString())
    }

    LaunchedEffect(personalApiKeyTextFieldState.text) {
        onPersonalApiKeyChange(personalApiKeyTextFieldState.text.toString())
    }

    Content(
        contentPadding = bottomPadding,
        instanceUrlTextFieldState = instanceUrlTextFieldState,
        instanceUrlTextFieldError = instanceUrlValidationError?.textFieldError,
        personalApiKeyTextFieldState = personalApiKeyTextFieldState,
        personalApiKeyTextFieldError = personalApiKeyValidationError?.textFieldError,
        isConnecting = isConnecting,
        onApiKeysLinkClick = { onApiKeysLinkClick(instanceUrlTextFieldState.text.toString()) },
    )
}

@Suppress("LongMethod")
@Composable
private fun Content(
    contentPadding: PaddingValues,
    instanceUrlTextFieldState: TextFieldState,
    instanceUrlTextFieldError: TextFieldError?,
    personalApiKeyTextFieldState: TextFieldState,
    personalApiKeyTextFieldError: TextFieldError?,
    isConnecting: Boolean,
    onApiKeysLinkClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        VerticalSpacer(16.dp)
        Text(
            text = stringResource(Res.string.login_screen_header),
            style = SnoutTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        VerticalSpacer(8.dp)
        Text(
            stringResource(Res.string.login_screen_description),
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
        )

        VerticalSpacer(24.dp)
        PrimaryTextField(
            state = instanceUrlTextFieldState,
            label = stringResource(Res.string.login_screen_instance_url_input_label),
            modifier = Modifier.fillMaxWidth(),
            error = instanceUrlTextFieldError,
            placeholder = stringResource(Res.string.login_screen_instance_url_input_placeholder),
            enabled = isConnecting.not(),
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
        )
        VerticalSpacer(8.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Selected whenever the field holds that cloud's URL, however it got there (chip, typing, a saved session).
            val instanceUrl = instanceUrlTextFieldState.text.toString()
            ChoiceChip(
                text = stringResource(Res.string.login_screen_cloud_us_chip),
                selected = instanceUrl.isSameUrl(PostHogCloudUsUrl),
                enabled = isConnecting.not(),
                onClick = { instanceUrlTextFieldState.setTextAndPlaceCursorAtEnd(PostHogCloudUsUrl) },
            )
            ChoiceChip(
                text = stringResource(Res.string.login_screen_cloud_eu_chip),
                selected = instanceUrl.isSameUrl(PostHogCloudEuUrl),
                enabled = isConnecting.not(),
                onClick = { instanceUrlTextFieldState.setTextAndPlaceCursorAtEnd(PostHogCloudEuUrl) },
            )
        }
        VerticalSpacer(16.dp)

        PrimaryTextField(
            state = personalApiKeyTextFieldState,
            label = stringResource(Res.string.login_screen_personal_api_key_input_label),
            modifier = Modifier.fillMaxWidth(),
            error = personalApiKeyTextFieldError,
            placeholder = stringResource(Res.string.login_screen_personal_api_key_input_placeholder),
            enabled = isConnecting.not(),
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
        )

        VerticalSpacer(8.dp)

        Text(
            text = stringResource(Res.string.login_screen_personal_api_key_create_in_web_button),
            modifier = Modifier.align(Alignment.End).clickable(onClick = onApiKeysLinkClick),
            textAlign = TextAlign.End,
            style = SnoutTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = SnoutTheme.colors.primary,
        )

        VerticalSpacer(16.dp)
        KeyScopes()
        VerticalSpacer(24.dp)
    }
}

/** Ignores case, surrounding spaces and a trailing slash, so "https://eu.posthog.com/" still counts as EU. */
private fun String.isSameUrl(url: String): Boolean = trim().trimEnd('/').equals(url, ignoreCase = true)

private val InstanceUrlValidationError.textFieldError: TextFieldError
    @Composable
    @ReadOnlyComposable
    get() = TextFieldError.Resource(
        when (this) {
            InstanceUrlValidationError.UrlIsEmpty ->
                Res.string.login_screen_instance_url_input_empty_error

            InstanceUrlValidationError.UrlIsInvalid ->
                Res.string.login_screen_instance_url_input_invalid_error
        },
    )

private val PersonalApiKeyValidationError.textFieldError: TextFieldError
    @Composable
    @ReadOnlyComposable
    get() = TextFieldError.Resource(
        when (this) {
            PersonalApiKeyValidationError.ApiKeyIsEmpty ->
                Res.string.login_screen_personal_api_key_input_empty_error

            PersonalApiKeyValidationError.ServerValidationFailed ->
                Res.string.login_screen_personal_api_key_input_unable_to_connect_error
        },
    )
