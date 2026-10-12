package com.nightcode.snout.shared.domain.data.auth

import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun getPersonalApiKey(): PersonalApiKey?
    suspend fun savePersonalApiKey(key: PersonalApiKey)
    suspend fun saveInstanceUrl(url: String)
    suspend fun getInstanceUrl(): String?
    suspend fun clear()
    fun observePersonalApiKey(): Flow<PersonalApiKey?>
}
