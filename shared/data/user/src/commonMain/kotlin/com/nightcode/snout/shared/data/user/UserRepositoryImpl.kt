package com.nightcode.snout.shared.data.user

import arrow.core.Either
import com.nightcode.snout.shared.api.users.UsersApi
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.user.mapper.toUser
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.User

internal class UserRepositoryImpl(private val api: UsersApi) : UserRepository {

    override suspend fun getCurrentUser(): Either<DataError, User> = api.getCurrentUser()
        .mapLeft { error -> error.toDataError() }
        .map { dto -> dto.toUser() }
}
