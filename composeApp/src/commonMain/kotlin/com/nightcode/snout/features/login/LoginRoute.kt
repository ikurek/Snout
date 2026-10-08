package com.nightcode.snout.features.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.StartupReady
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object LoginNavKey : NavKey

@Composable
internal fun LoginRoute(onUserAuthorized: () -> Unit) {
    val viewModel = koinViewModel<LoginViewModel>()
    val instanceUrlValidationError by viewModel.instanceUrlValidationError.collectAsStateWithLifecycle()
    val personalApiKeyTextValidationError by viewModel.personalApiKeyValidationError.collectAsStateWithLifecycle()
    val isConnectEnabled by viewModel.isConnectEnabled.collectAsStateWithLifecycle()
    val isConnecting by viewModel.isConnecting.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    StartupReady()

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                is LoginSideEffect.UserAuthorized -> onUserAuthorized()
                is LoginSideEffect.ShowError -> snackbarHostState.showSnackbar(getString(sideEffect.message))
                is LoginSideEffect.OpenUrl -> uriHandler.openUri(sideEffect.url)
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
        onApiKeysLinkClick = viewModel::onCreateApiKeyClick,
        onConnectClick = viewModel::onConnectClick,
    )
}
