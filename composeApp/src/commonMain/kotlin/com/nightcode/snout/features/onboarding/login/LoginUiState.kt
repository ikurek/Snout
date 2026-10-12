package com.nightcode.snout.features.onboarding.login

import com.nightcode.snout.shared.domain.model.auth.InstanceUrlValidationError
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
import com.nightcode.snout.shared.domain.model.common.DataError

/** The typed values live in text field states owned by the route; this holds what the checks made of them. */
internal data class LoginUiState(
    /** The instance from an earlier session that lost its key, to fill in so only the key needs typing. */
    val savedInstanceUrl: String? = null,
    val instanceUrlValidationError: InstanceUrlValidationError? = null,
    val personalApiKeyValidationError: PersonalApiKeyValidationError? = null,
    val isConnecting: Boolean = false,
) {
    val isConnectEnabled: Boolean
        get() = instanceUrlValidationError == null && personalApiKeyValidationError == null
}

internal sealed interface LoginSideEffect {

    data object UserAuthorized : LoginSideEffect

    /** Connecting failed for a reason other than a rejected key, which shows on the key field instead. */
    data class ConnectFailed(val error: DataError) : LoginSideEffect
}
