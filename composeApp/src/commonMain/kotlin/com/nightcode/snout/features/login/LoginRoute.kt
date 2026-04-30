package com.nightcode.snout.features.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.url_posthog_api_keys_url

@Serializable
internal data object LoginNavKey : NavKey

@Composable
internal fun LoginRoute() {
    val viewModel = koinViewModel<LoginViewModel>()
    val instanceUrlValidationError by
        viewModel.instanceUrlValidationError.collectAsStateWithLifecycle()
    val personalApiKeyTextValidationError by viewModel.personalApiKeyValidationError.collectAsStateWithLifecycle()
    val isConnectEnabled by viewModel.isConnectEnabled.collectAsStateWithLifecycle()
    val isConnecting by viewModel.isConnecting.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val personalApiKeysUrl = stringResource(Res.string.url_posthog_api_keys_url)

    LoginScreen(
        instanceUrlValidationError = instanceUrlValidationError,
        personalApiKeyValidationError = personalApiKeyTextValidationError,
        isConnectEnabled = isConnectEnabled,
        isConnecting = isConnecting,
        onInstanceUrlChange = viewModel::onInstanceUrlChange,
        onPersonalApiKeyChange = viewModel::onPersonalApiKeyChanged,
        onApiKeysLinkClick = { uriHandler.openUri(personalApiKeysUrl) },
        onConnectClick = viewModel::onConnectClick,
    )
}
