package com.nightcode.snout.shared.domain.data.links

import kotlinx.coroutines.flow.Flow

/** The link Snout was asked to open and hasn't opened yet. */
interface IncomingLinkRepository {
    fun observePendingLink(): Flow<String?>
    suspend fun savePendingLink(url: String)
    suspend fun clearPendingLink()
}
