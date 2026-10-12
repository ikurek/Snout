package com.nightcode.snout.shared.data.testutils

import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.storage.database.JsonCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** [JsonCache] kept in a map, so repositories can be tested against a real [JsonCacheStore] without a database. */
class InMemoryJsonCache : JsonCache {
    private val payloads = MutableStateFlow(emptyMap<String, String>())

    override fun observe(key: String): Flow<String?> = payloads.map { it[key] }.distinctUntilChanged()

    override suspend fun put(key: String, payload: String) = payloads.update { it + (key to payload) }

    override suspend fun clear() {
        payloads.value = emptyMap()
    }
}

/** A [JsonCacheStore] over an empty [InMemoryJsonCache], configured like the app's. */
fun testJsonCacheStore(): JsonCacheStore = JsonCacheStore(TestJson, InMemoryJsonCache())
