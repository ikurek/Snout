package com.nightcode.snout.shared.domain.usecase.user

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.User

class GetCurrentUser(private val repository: UserRepository) {
    suspend operator fun invoke(): Either<DataError, User> = repository.getCurrentUser()
}
