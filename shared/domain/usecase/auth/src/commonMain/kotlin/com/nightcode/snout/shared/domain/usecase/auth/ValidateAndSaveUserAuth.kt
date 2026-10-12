package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.domain.model.common.DataError
import org.koin.core.annotation.Single

/** Saves the connection, then checks it by fetching the account, which is stored for Settings on success. */
@Single
class ValidateAndSaveUserAuth(private val authRepository: AuthRepository, private val userRepository: UserRepository) {
    suspend operator fun invoke(token: String, instanceUrl: String): Either<DataError, Unit> = either {
        authRepository.savePersonalApiKey(PersonalApiKey(token))
        authRepository.saveInstanceUrl(instanceUrl)
        userRepository.refreshCurrentUser().onLeft { authRepository.clear() }.bind()
    }
}
