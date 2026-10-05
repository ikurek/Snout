package com.nightcode.snout.shared.domain.data.appstate

/** Cached API payloads kept for the signed-in account. */
interface CacheRepository {
    suspend fun clear()
}
