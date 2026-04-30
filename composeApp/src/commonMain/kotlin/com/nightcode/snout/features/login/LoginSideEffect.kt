package com.nightcode.snout.features.login

internal sealed interface LoginSideEffect {

    data object UserAuthorized : LoginSideEffect

    data object UserAuthorizationFailed : LoginSideEffect
}
