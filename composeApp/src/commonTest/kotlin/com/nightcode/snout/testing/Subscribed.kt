package com.nightcode.snout.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Runs [block] while [this] has a collector on the main dispatcher, so `WhileSubscribed` state flows stay live.
 * The collector stops when [block] ends, also when an assertion in it fails.
 */
internal suspend fun <T> Flow<T>.subscribed(block: suspend () -> Unit) = coroutineScope {
    val collector = launch(Dispatchers.Main) { collect {} }
    try {
        block()
    } finally {
        collector.cancel()
    }
}
