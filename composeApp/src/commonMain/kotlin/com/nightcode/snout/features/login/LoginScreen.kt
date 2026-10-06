package com.nightcode.snout.features.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.AppIcon
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.atoms.chips.ActionChip
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextField
import com.nightcode.snout.designsystem.atoms.textfields.TextFieldError
import com.nightcode.snout.designsystem.molecules.InfoNote
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.auth.InstanceUrlValidationError
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.app_name
import snout.composeapp.generated.resources.ic_arrow_right_alt_24dp
import snout.composeapp.generated.resources.login_screen_app_name_subtitle
import snout.composeapp.generated.resources.login_screen_cloud_eu_chip
import snout.composeapp.generated.resources.login_screen_cloud_us_chip
import snout.composeapp.generated.resources.login_screen_connect_button_text
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
import snout.composeapp.generated.resources.login_screen_scopes_hint

@Composable
internal fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    instanceUrlValidationError: InstanceUrlValidationError?,
    personalApiKeyValidationError: PersonalApiKeyValidationError?,
    isConnectEnabled: Boolean,
    isConnecting: Boolean,
    onInstanceUrlChange: (instanceUrl: String) -> Unit,
    onPersonalApiKeyChange: (personalApiKey: String) -> Unit,
    onApiKeysLinkClick: (instanceUrl: String) -> Unit,
    onConnectClick: (instanceUrl: String, personalApiKey: String) -> Unit,
) {
    val instanceUrlTextFieldState: TextFieldState = rememberTextFieldState()
    val personalApiKeyTextFieldState: TextFieldState = rememberTextFieldState()

    LaunchedEffect(instanceUrlTextFieldState.text) {
        onInstanceUrlChange(instanceUrlTextFieldState.text.toString())
    }

    LaunchedEffect(personalApiKeyTextFieldState.text) {
        onPersonalApiKeyChange(personalApiKeyTextFieldState.text.toString())
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar() },
        bottomBar = {
            BottomBar(
                isConnecting = isConnecting,
                isConnectEnabled = isConnectEnabled,
                onClick = {
                    onConnectClick(
                        instanceUrlTextFieldState.text.toString(),
                        personalApiKeyTextFieldState.text.toString(),
                    )
                },
            )
        },
    ) { contentPadding ->
        Content(
            contentPadding = contentPadding,
            instanceUrlTextFieldState = instanceUrlTextFieldState,
            instanceUrlTextFieldError = instanceUrlValidationError?.textFieldError,
            personalApiKeyTextFieldState = personalApiKeyTextFieldState,
            personalApiKeyTextFieldError = personalApiKeyValidationError?.textFieldError,
            isConnecting = isConnecting,
            onApiKeysLinkClick = { onApiKeysLinkClick(instanceUrlTextFieldState.text.toString()) },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar() {
    TopAppBar(
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 12.dp, end = 10.dp)) { AppIcon() }
        },
        title = { Text(text = stringResource(Res.string.app_name)) },
        subtitle = { Text(text = stringResource(Res.string.login_screen_app_name_subtitle)) },
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .imePadding(),
    ) {
        VerticalSpacer(24.dp)

        Text(
            stringResource(Res.string.login_screen_header),
            style = SnoutTheme.typography.displaySmall,
        )
        VerticalSpacer(8.dp)
        Text(
            stringResource(Res.string.login_screen_description),
            style = SnoutTheme.typography.bodyLarge,
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
            ActionChip(
                text = stringResource(Res.string.login_screen_cloud_us_chip),
                enabled = isConnecting.not(),
                onClick = { instanceUrlTextFieldState.setTextAndPlaceCursorAtEnd(PostHogCloudUsUrl) },
            )
            ActionChip(
                text = stringResource(Res.string.login_screen_cloud_eu_chip),
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
            style = SnoutTheme.typography.labelMedium,
            color = SnoutTheme.colors.primary,
        )

        VerticalSpacer(16.dp)
        InfoNote(text = stringResource(Res.string.login_screen_scopes_hint))
        VerticalSpacer(24.dp)
    }
}

@Composable
private fun BottomBar(isConnecting: Boolean, isConnectEnabled: Boolean, onClick: () -> Unit) {
    PrimaryButton(
        text = stringResource(Res.string.login_screen_connect_button_text),
        onClick = onClick,
        loading = isConnecting,
        enabled = isConnectEnabled,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
        icon = {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right_alt_24dp),
                contentDescription = null,
            )
        },
    )
}

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

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LoginScreenEmptyPreview() = LoginScreen(
    snackbarHostState = remember { SnackbarHostState() },
    instanceUrlValidationError = null,
    personalApiKeyValidationError = null,
    isConnectEnabled = true,
    isConnecting = false,
    onInstanceUrlChange = {},
    onPersonalApiKeyChange = {},
    onApiKeysLinkClick = {},
    onConnectClick = { _, _ -> },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LoginScreenErrorsPreview() = LoginScreen(
    snackbarHostState = remember { SnackbarHostState() },
    instanceUrlValidationError = InstanceUrlValidationError.UrlIsInvalid,
    personalApiKeyValidationError = PersonalApiKeyValidationError.ServerValidationFailed,
    isConnectEnabled = true,
    isConnecting = false,
    onInstanceUrlChange = {},
    onPersonalApiKeyChange = {},
    onApiKeysLinkClick = {},
    onConnectClick = { _, _ -> },
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LoginScreenConnectingPreview() = LoginScreen(
    snackbarHostState = remember { SnackbarHostState() },
    instanceUrlValidationError = null,
    personalApiKeyValidationError = null,
    isConnectEnabled = false,
    isConnecting = true,
    onInstanceUrlChange = {},
    onPersonalApiKeyChange = {},
    onApiKeysLinkClick = {},
    onConnectClick = { _, _ -> },
)
