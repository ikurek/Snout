package com.nightcode.snout.shared.data.common

import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.storage.database.JsonCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/** Typed facade over [JsonCache]: repositories map API responses to domain models and store those as JSON. */
class JsonCacheStore(val json: Json, private val cache: JsonCache) : CacheRepository {

    inline fun <reified T> observe(key: String): Flow<T?> = observeRaw(key).map { raw ->
        raw?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() }
    }

    suspend inline fun <reified T> put(key: String, value: T) = putRaw(key, json.encodeToString(value))

    fun observeRaw(key: String): Flow<String?> = cache.observe(key)

    suspend fun putRaw(key: String, payload: String) = cache.put(key, payload)

    override suspend fun clear() = cache.clear()
}
