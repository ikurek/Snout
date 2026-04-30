package com.nightcode.snout.shared.domain.data.user

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError
import com.nightcode.snout.shared.domain.model.user.User

interface UserRepository {
    suspend fun getCurrentUser(): Either<GetCurrentUserError, User>
}
