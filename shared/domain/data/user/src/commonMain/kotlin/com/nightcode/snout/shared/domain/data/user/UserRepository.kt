package com.nightcode.snout.shared.domain.data.user

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** The signed-in account as last fetched; null before the first fetch and after signing out. */
    fun observeCurrentUser(): Flow<User?>

    /** Fetches the signed-in account from PostHog and stores it for [observeCurrentUser]. */
    suspend fun refreshCurrentUser(): Either<DataError, Unit>

    suspend fun clear()
}
