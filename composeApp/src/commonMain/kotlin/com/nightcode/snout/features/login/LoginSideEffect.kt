package com.nightcode.snout.features.login

import kotlin.time.Instant

internal sealed interface LoginSideEffect {

    data class UserAuthorized(val firstName: String, val dateJoined: Instant) : LoginSideEffect

    data object UserAuthorizationFailed : LoginSideEffect
}
