package com.nightcode.snout.features.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.snackbar_generic_error_message
import snout.composeapp.generated.resources.url_posthog_api_keys_url
import kotlin.time.Instant

@Serializable
internal data object LoginNavKey : NavKey

@Composable
internal fun LoginRoute(onUserAuthorized: (firstName: String, dateJoined: Instant) -> Unit) {
    val viewModel = koinViewModel<LoginViewModel>()
    val instanceUrlValidationError by
        viewModel.instanceUrlValidationError.collectAsStateWithLifecycle()
    val personalApiKeyTextValidationError by viewModel.personalApiKeyValidationError.collectAsStateWithLifecycle()
    val isConnectEnabled by viewModel.isConnectEnabled.collectAsStateWithLifecycle()
    val isConnecting by viewModel.isConnecting.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val personalApiKeysUrl = stringResource(Res.string.url_posthog_api_keys_url)
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarErrorMessage = stringResource(Res.string.snackbar_generic_error_message)

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                is LoginSideEffect.UserAuthorized -> onUserAuthorized(
                    sideEffect.firstName,
                    sideEffect.dateJoined,
                )

                is LoginSideEffect.UserAuthorizationFailed -> snackbarHostState.showSnackbar(
                    snackbarErrorMessage,
                )
            }
        }
    }

    LoginScreen(
        snackbarHostState = snackbarHostState,
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
