package com.nightcode.snout.shared.domain.data.auth

import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey

interface AuthRepository {
    suspend fun getPersonalApiKey(): PersonalApiKey?
    suspend fun savePersonalApiKey(key: PersonalApiKey)
    suspend fun saveInstanceUrl(url: String)
    suspend fun clear()
}
