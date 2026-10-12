package com.nightcode.snout.common.state

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class RefreshableUiStateTest :
    ShouldSpec({
        val noRefreshes = emptyFlow<Unit>()

        /** Fetches answer in turn with [results]; each call records the key it was asked for. */
        class Fetcher(vararg results: Either<DataError, List<String>>) {
            private val pending = results.toMutableList()
            val keys = mutableListOf<Int>()

            suspend fun fetch(key: Int): Either<DataError, List<String>> {
                keys += key
                return pending.removeFirst()
            }
        }

        fun Flow<Int>.states(refreshes: Flow<Unit>, fetcher: Fetcher) =
            refreshableUiStates(refreshes, isEmpty = { it.isEmpty() }, fetch = fetcher::fetch)

        should("show the skeleton on the first load, then the content") {
            val fetcher = Fetcher(Either.Right(listOf("a")))
            flowOf(1).states(noRefreshes, fetcher).toList() shouldBe listOf(
                RefreshableUiState.Loading,
                RefreshableUiState.Content(listOf("a"), isRefreshing = false),
            )
        }

        should("show the error placeholder when the first load fails") {
            val fetcher = Fetcher(Either.Left(DataError.ConnectionError))
            flowOf(1).states(noRefreshes, fetcher).toList() shouldBe listOf(
                RefreshableUiState.Loading,
                RefreshableUiState.Error(DataError.ConnectionError),
            )
        }

        should("show an empty list as empty") {
            flowOf(1).states(noRefreshes, Fetcher(Either.Right(emptyList()))).toList() shouldBe listOf(
                RefreshableUiState.Loading,
                RefreshableUiState.Empty(isRefreshing = false),
            )
        }

        should("keep the content while reloading the same key and show a failed reload over it") {
            val fetcher = Fetcher(Either.Right(listOf("a")), Either.Left(DataError.RateLimited))
            val keys = MutableSharedFlow<Int>()
            recording(keys.states(noRefreshes, fetcher)) { states ->
                keys.awaitSubscriber()
                keys.emit(1)
                states.awaitSize(2)
                keys.awaitSubscriber()
                keys.emit(1)
                states.awaitSize(4) shouldBe listOf(
                    RefreshableUiState.Loading,
                    RefreshableUiState.Content(listOf("a"), isRefreshing = false),
                    RefreshableUiState.Content(listOf("a"), isRefreshing = true),
                    RefreshableUiState.Content(listOf("a"), isRefreshing = false, refreshError = DataError.RateLimited),
                )
            }
        }

        should("start from the skeleton for another key") {
            val fetcher = Fetcher(Either.Right(listOf("a")), Either.Right(listOf("b")))
            val keys = MutableSharedFlow<Int>()
            recording(keys.states(noRefreshes, fetcher)) { states ->
                keys.awaitSubscriber()
                keys.emit(1)
                states.awaitSize(2)
                keys.emit(2)
                states.awaitSize(4) shouldBe listOf(
                    RefreshableUiState.Loading,
                    RefreshableUiState.Content(listOf("a"), isRefreshing = false),
                    RefreshableUiState.Loading,
                    RefreshableUiState.Content(listOf("b"), isRefreshing = false),
                )
            }
            fetcher.keys shouldBe listOf(1, 2)
        }

        should("load the latest key again on a refresh") {
            val fetcher = Fetcher(Either.Right(listOf("a")), Either.Right(listOf("a", "b")))
            val refreshes = MutableSharedFlow<Unit>()
            recording(flowOf(1).states(refreshes, fetcher)) { states ->
                states.awaitSize(2)
                refreshes.awaitSubscriber()
                refreshes.emit(Unit)
                states.awaitSize(4) shouldBe listOf(
                    RefreshableUiState.Loading,
                    RefreshableUiState.Content(listOf("a"), isRefreshing = false),
                    RefreshableUiState.Content(listOf("a"), isRefreshing = true),
                    RefreshableUiState.Content(listOf("a", "b"), isRefreshing = false),
                )
            }
            fetcher.keys shouldBe listOf(1, 1)
        }

        should("cancel a load still running when a newer one starts") {
            val keys = MutableSharedFlow<Int>()
            var firstCancelled = false
            val states = keys.refreshableUiStates(noRefreshes, isEmpty = { false }) { key ->
                if (key == 1) {
                    try {
                        awaitCancellation()
                    } finally {
                        firstCancelled = true
                    }
                }
                Either.Right("loaded $key")
            }
            recording(states) { recorded ->
                keys.awaitSubscriber()
                keys.emit(1)
                recorded.awaitSize(1)
                keys.emit(2)
                recorded.awaitSize(3) shouldBe listOf(
                    RefreshableUiState.Loading,
                    RefreshableUiState.Loading,
                    RefreshableUiState.Content("loaded 2", isRefreshing = false),
                )
            }
            firstCancelled shouldBe true
        }
    })

/** Collects [flow] while [block] runs, so it can emit inputs and wait for the states they lead to. */
private suspend fun <T> recording(flow: Flow<T>, block: suspend (Recorded<T>) -> Unit) = coroutineScope {
    val recorded = Recorded<T>()
    val collector = launch(start = CoroutineStart.UNDISPATCHED) { flow.collect { recorded.add(it) } }
    try {
        block(recorded)
    } finally {
        collector.cancel()
    }
}

private class Recorded<T> {
    private val values = MutableStateFlow(emptyList<T>())

    fun add(value: T) = values.update { it + value }

    suspend fun awaitSize(size: Int): List<T> = withTimeout(AwaitTimeout) { values.first { it.size >= size } }
}

private suspend fun MutableSharedFlow<*>.awaitSubscriber() {
    withTimeout(AwaitTimeout) { subscriptionCount.first { it > 0 } }
}

private val AwaitTimeout = 5.seconds
