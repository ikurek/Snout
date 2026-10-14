package com.nightcode.snout.shared.data.user

import arrow.core.Either
import com.nightcode.snout.shared.api.users.UsersApi
import com.nightcode.snout.shared.data.user.mapper.toStoredUser
import com.nightcode.snout.shared.data.user.mapper.toUser
import com.nightcode.snout.shared.data.user.model.StoredUser
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.User
import com.nightcode.snout.shared.storage.securestore.SecureStore
import com.nightcode.snout.shared.storage.securestore.SecureStoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

internal const val CurrentUserKey = "current_user"

/**
 * Keeps the signed-in account in the [SecureStore] (it is personal data), so Settings can show it at once and
 * offline. Loaded once, then served from memory.
 */
@Single(binds = [UserRepository::class])
internal class UserRepositoryImpl(
    private val api: UsersApi,
    private val secureStore: SecureStore,
    private val json: Json,
) : UserRepository {

    private val currentUser = MutableStateFlow<User?>(null)
    private val mutex = Mutex()
    private var isLoaded = false

    override fun observeCurrentUser(): Flow<User?> = flow {
        withCurrentUser { }
        emitAll(currentUser)
    }

    override suspend fun refreshCurrentUser(): Either<DataError, Unit> = api.getCurrentUser()
        .map { dto -> store(dto.toUser()) }

    override suspend fun clear() = withCurrentUser {
        secureStore.remove(CurrentUserKey)
        currentUser.value = null
    }

    private suspend fun store(user: User) = withCurrentUser {
        currentUser.value = user
        try {
            secureStore.set(CurrentUserKey, json.encodeToString(StoredUser.serializer(), user.toStoredUser()))
        } catch (_: SecureStoreException) {
            // Only costs the next launch a fetch; the account still shows from memory.
        }
    }

    private suspend fun <T> withCurrentUser(block: suspend () -> T): T = mutex.withLock {
        if (!isLoaded) {
            currentUser.value = load()
            isLoaded = true
        }
        block()
    }

    /** A record that no longer decodes (from another app version, or corrupted) is dropped. */
    private suspend fun load(): User? {
        val stored = secureStore.get(CurrentUserKey) ?: return null
        val user = try {
            json.decodeFromString(StoredUser.serializer(), stored).toUser()
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
        if (user == null) secureStore.remove(CurrentUserKey)
        return user
    }
}
