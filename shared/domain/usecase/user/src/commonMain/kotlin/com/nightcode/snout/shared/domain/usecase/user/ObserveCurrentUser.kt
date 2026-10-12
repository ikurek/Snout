package com.nightcode.snout.shared.domain.usecase.user

import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.user.User
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/** The signed-in account as last fetched; null until [RefreshCurrentUser] first succeeds. */
@Single
class ObserveCurrentUser(private val repository: UserRepository) {
    operator fun invoke(): Flow<User?> = repository.observeCurrentUser()
}
