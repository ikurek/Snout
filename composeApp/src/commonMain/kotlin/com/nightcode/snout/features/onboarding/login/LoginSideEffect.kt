package com.nightcode.snout.features.onboarding.login

import org.jetbrains.compose.resources.StringResource

internal sealed interface LoginSideEffect {

    data object UserAuthorized : LoginSideEffect

    data class ShowError(val message: StringResource) : LoginSideEffect

    data class OpenUrl(val url: String) : LoginSideEffect
}
