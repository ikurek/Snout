package com.nightcode.snout.shared.storage.database

import kotlinx.coroutines.flow.Flow

/** Key-value store for serialized API payloads so lists open instantly and work offline. */
interface JsonCache {
    fun observe(key: String): Flow<String?>
    suspend fun put(key: String, payload: String)
    suspend fun clear()
}
