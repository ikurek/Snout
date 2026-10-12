package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository

/**
 * Queues the link in [text] (an opened URL, shared or pasted text) to be opened. It stays pending until the app
 * has a Home screen to open it from, so a link that arrives during a cold start or onboarding isn't lost.
 */
class SubmitIncomingLink(private val findLinkInText: FindLinkInText, private val repository: IncomingLinkRepository) {
    suspend operator fun invoke(text: String) {
        findLinkInText(text).onRight { repository.savePendingLink(it) }
    }
}
