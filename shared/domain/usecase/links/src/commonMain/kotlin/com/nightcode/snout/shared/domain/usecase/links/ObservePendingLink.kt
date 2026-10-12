package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import kotlinx.coroutines.flow.Flow

class ObservePendingLink(private val repository: IncomingLinkRepository) {
    operator fun invoke(): Flow<String?> = repository.observePendingLink()
}
