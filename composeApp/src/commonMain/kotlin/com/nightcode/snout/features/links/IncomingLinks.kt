package com.nightcode.snout.features.links

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Carries links from the platform host (an Android intent, an iOS URL) into the app, which queues them with
 * `SubmitIncomingLink`. Koin only starts with the app's first composition, so a link that arrives on launch
 * can't reach the use case straight from the host; the channel holds it until then.
 */
class IncomingLinks {
    private val links = Channel<String>(Channel.UNLIMITED)

    /** [text] is a URL, or shared text with a link somewhere in it. */
    fun open(text: String) {
        links.trySend(text)
    }

    internal fun receive(): Flow<String> = links.receiveAsFlow()
}
