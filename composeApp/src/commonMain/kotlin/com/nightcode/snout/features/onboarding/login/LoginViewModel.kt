package com.nightcode.snout.features.onboarding.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.ValidateAndSaveUserAuth
import com.nightcode.snout.shared.domain.usecase.auth.ValidateInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.ValidatePersonalApiKey
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

internal const val PostHogCloudUsUrl = "https://us.posthog.com"
internal const val PostHogCloudEuUrl = "https://eu.posthog.com"
private const val ApiKeysSettingsPath = "/settings/user-api-keys"

@KoinViewModel
internal class LoginViewModel(
    private val validateInstanceUrl: ValidateInstanceUrl,
    private val validatePersonalApiKey: ValidatePersonalApiKey,
    private val validateAndSaveUserAuth: ValidateAndSaveUserAuth,
    private val openInBrowser: OpenInBrowser,
    getInstanceUrl: GetInstanceUrl,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _sideEffects: Channel<LoginSideEffect> = Channel()
    val sideEffects: Flow<LoginSideEffect> = _sideEffects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val savedInstanceUrl = getInstanceUrl()
            _uiState.update { it.copy(savedInstanceUrl = savedInstanceUrl) }
        }
    }

    fun onInstanceUrlChanged(instanceUrl: String) = _uiState.update { state ->
        state.copy(
            instanceUrlValidationError = state.instanceUrlValidationError.takeIf { instanceUrl.isBlank() },
            // Another instance may accept the same key, so its rejection no longer stands. An empty key still does.
            personalApiKeyValidationError = state.personalApiKeyValidationError
                .takeUnless { it == PersonalApiKeyValidationError.ServerValidationFailed },
        )
    }

    fun onPersonalApiKeyChanged(personalApiKey: String) = _uiState.update { state ->
        state.copy(
            personalApiKeyValidationError = state.personalApiKeyValidationError.takeIf { personalApiKey.isBlank() },
        )
    }

    /** Opens the page where the user creates a key, on the instance typed so far or else the US cloud. */
    fun openApiKeySettings(instanceUrl: String) {
        val baseUrl = validateInstanceUrl(instanceUrl).getOrNull() ?: PostHogCloudUsUrl
        openInBrowser(baseUrl + ApiKeysSettingsPath)
    }

    fun connect(instanceUrl: String, personalApiKey: String) = viewModelScope.launch {
        _uiState.update { it.copy(isConnecting = true) }
        val normalizedUrl = validateInstanceUrl(instanceUrl)
            .onLeft { error -> _uiState.update { it.copy(instanceUrlValidationError = error) } }
            .getOrNull()
        if (normalizedUrl == null) {
            _uiState.update { it.copy(isConnecting = false) }
            return@launch
        }
        validatePersonalApiKey(personalApiKey).onLeft { error ->
            _uiState.update { it.copy(personalApiKeyValidationError = error, isConnecting = false) }
            return@launch
        }
        validateAndSaveUserAuth(token = personalApiKey.trim(), instanceUrl = normalizedUrl)
            .onLeft { error ->
                when (error) {
                    DataError.Unauthorized, DataError.Forbidden -> _uiState.update {
                        it.copy(personalApiKeyValidationError = PersonalApiKeyValidationError.ServerValidationFailed)
                    }

                    DataError.RateLimited,
                    DataError.PaymentRequired,
                    DataError.NotFound,
                    DataError.ConnectionError,
                    DataError.UnexpectedError,
                    -> _sideEffects.send(LoginSideEffect.ConnectFailed(error))
                }
            }
            .onRight { _sideEffects.send(LoginSideEffect.UserAuthorized) }
        _uiState.update { it.copy(isConnecting = false) }
    }
}
