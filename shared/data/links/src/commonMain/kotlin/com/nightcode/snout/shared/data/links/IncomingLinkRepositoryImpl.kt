package com.nightcode.snout.shared.data.links

import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Kept in memory only: a link is opened right away, or dropped with the process. */
internal class IncomingLinkRepositoryImpl : IncomingLinkRepository {
    private val pendingLink = MutableStateFlow<String?>(null)

    override fun observePendingLink(): Flow<String?> = pendingLink

    override suspend fun savePendingLink(url: String) {
        pendingLink.value = url
    }

    override suspend fun clearPendingLink() {
        pendingLink.value = null
    }
}
