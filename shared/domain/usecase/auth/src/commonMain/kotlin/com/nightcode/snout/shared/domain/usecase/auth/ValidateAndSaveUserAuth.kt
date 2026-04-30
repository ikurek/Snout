package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError
import com.nightcode.snout.shared.domain.model.user.User

class ValidateAndSaveUserAuth(private val authRepository: AuthRepository, private val userRepository: UserRepository) {
    suspend operator fun invoke(token: String, instanceUrl: String): Either<GetCurrentUserError, User> = either {
        authRepository.savePersonalApiKey(PersonalApiKey(token))
        authRepository.saveInstanceUrl(instanceUrl)
        userRepository.getCurrentUser().onLeft { authRepository.clear() }.bind()
    }
}
