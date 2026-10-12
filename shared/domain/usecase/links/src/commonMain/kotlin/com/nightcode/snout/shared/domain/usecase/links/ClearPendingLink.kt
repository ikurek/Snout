package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import org.koin.core.annotation.Single

@Single
class ClearPendingLink(private val repository: IncomingLinkRepository) {
    suspend operator fun invoke() = repository.clearPendingLink()
}
