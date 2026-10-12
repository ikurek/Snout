package com.nightcode.snout.shared.data.testutils

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/** How long [emitsDuring] waits for an emission that [block] may have sent. */
private val EmissionTimeout = 500.milliseconds

/** Whether this flow (a repository's `changes`) emits while [block] runs, subscribing before [block] starts. */
suspend fun Flow<Unit>.emitsDuring(block: suspend () -> Unit): Boolean = coroutineScope {
    val emission = async(start = CoroutineStart.UNDISPATCHED) { first() }
    block()
    val emitted = withTimeoutOrNull(EmissionTimeout) { emission.await() } != null
    emission.cancel()
    emitted
}
