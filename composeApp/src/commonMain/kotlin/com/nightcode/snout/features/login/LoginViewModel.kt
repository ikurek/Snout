package com.nightcode.snout.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.auth.InstanceUrlValidationError
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError
import com.nightcode.snout.shared.domain.usecase.auth.ValidateAndSaveUserAuth
import com.nightcode.snout.shared.domain.usecase.auth.ValidateInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.ValidatePersonalApiKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.snackbar_generic_error_message
import snout.composeapp.generated.resources.snackbar_rate_limited_message

internal const val PostHogCloudUsUrl = "https://us.posthog.com"
internal const val PostHogCloudEuUrl = "https://eu.posthog.com"
private const val ApiKeysSettingsPath = "/settings/user-api-keys"

@KoinViewModel
internal class LoginViewModel(
    private val validateInstanceUrl: ValidateInstanceUrl,
    private val validatePersonalApiKey: ValidatePersonalApiKey,
    private val validateAndSaveUserAuth: ValidateAndSaveUserAuth,
) : ViewModel() {

    private val _sideEffects: Channel<LoginSideEffect> = Channel()
    val sideEffects: Flow<LoginSideEffect> = _sideEffects.receiveAsFlow()

    private val _isConnecting: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _instanceUrlValidationError = MutableStateFlow<InstanceUrlValidationError?>(null)
    val instanceUrlValidationError = _instanceUrlValidationError.asStateFlow()

    private val _personalApiKeyValidationError =
        MutableStateFlow<PersonalApiKeyValidationError?>(null)
    val personalApiKeyValidationError = _personalApiKeyValidationError.asStateFlow()

    val isConnectEnabled = combine(
        _instanceUrlValidationError,
        _personalApiKeyValidationError,
    ) { instanceUrlValidationError, personalApiKeyValidationError ->
        instanceUrlValidationError == null && personalApiKeyValidationError == null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    fun onInstanceUrlChange(instanceUrl: String) {
        if (instanceUrl.isNotBlank()) {
            _instanceUrlValidationError.value = null
        }
    }

    fun onPersonalApiKeyChanged(personalApiKey: String) {
        if (personalApiKey.isNotBlank()) {
            _personalApiKeyValidationError.value = null
        }
    }

    fun onCreateApiKeyClick(instanceUrl: String) = viewModelScope.launch {
        val baseUrl = validateInstanceUrl(instanceUrl).getOrNull() ?: PostHogCloudUsUrl
        _sideEffects.send(LoginSideEffect.OpenUrl(baseUrl + ApiKeysSettingsPath))
    }

    fun onConnectClick(instanceUrl: String, personalApiKey: String) = viewModelScope.launch {
        _isConnecting.value = true
        val normalizedUrl = validateInstanceUrl(instanceUrl)
            .onLeft { _instanceUrlValidationError.value = it }
            .getOrNull()
        if (normalizedUrl == null) {
            _isConnecting.value = false
            return@launch
        }
        validatePersonalApiKey(personalApiKey).onLeft {
            _personalApiKeyValidationError.value = it
            _isConnecting.value = false
            return@launch
        }
        validateAndSaveUserAuth(token = personalApiKey.trim(), instanceUrl = normalizedUrl)
            .onLeft { error ->
                when (error) {
                    GetCurrentUserError.Unauthorized,
                    GetCurrentUserError.Forbidden,
                    ->
                        _personalApiKeyValidationError.value =
                            PersonalApiKeyValidationError.ServerValidationFailed

                    GetCurrentUserError.RateLimited ->
                        _sideEffects.send(LoginSideEffect.ShowError(Res.string.snackbar_rate_limited_message))

                    GetCurrentUserError.ConnectionError,
                    GetCurrentUserError.UnexpectedError,
                    ->
                        _sideEffects.send(LoginSideEffect.ShowError(Res.string.snackbar_generic_error_message))
                }
            }
            .onRight { _sideEffects.send(LoginSideEffect.UserAuthorized) }
        _isConnecting.value = false
    }
}
