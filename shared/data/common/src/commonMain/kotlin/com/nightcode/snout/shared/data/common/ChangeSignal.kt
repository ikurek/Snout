package com.nightcode.snout.shared.data.common

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Backs a repository's `changes`: [emit] after a successful mutation tells whoever shows that data to load it again.
 * It carries no data and replays nothing, so only screens open at the time reload; others load fresh when opened.
 */
class ChangeSignal {
    private val signal = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val changes: Flow<Unit> = signal.asSharedFlow()

    fun emit() {
        signal.tryEmit(Unit)
    }
}
