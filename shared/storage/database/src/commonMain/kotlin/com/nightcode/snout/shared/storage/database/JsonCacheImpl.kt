package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

internal class JsonCacheImpl(private val database: SnoutDatabase, private val ioDispatcher: CoroutineDispatcher) :
    JsonCache {

    override fun observe(key: String): Flow<String?> = database.cachedPayloadQueries
        .selectByKey(key)
        .asFlow()
        .mapToOneOrNull(ioDispatcher)
        .map { it?.payload }

    override suspend fun put(key: String, payload: String) {
        withContext(ioDispatcher) {
            database.cachedPayloadQueries.upsert(key, payload, Clock.System.now().toEpochMilliseconds())
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) { database.cachedPayloadQueries.deleteAll() }
    }
}
