package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObservePendingLink(private val repository: IncomingLinkRepository) {
    operator fun invoke(): Flow<String?> = repository.observePendingLink()
}
