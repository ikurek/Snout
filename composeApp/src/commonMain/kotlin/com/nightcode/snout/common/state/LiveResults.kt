package com.nightcode.snout.common.state

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration

/**
 * Fetches a list live for every filter that [isLive] accepts, cancelling the previous request. Emits `null` for the
 * other filters, so the screen falls back to its cached list.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun <F, T> Flow<F>.liveResults(
    isLive: (F) -> Boolean,
    fetch: suspend (F) -> Either<DataError, List<T>>,
): Flow<CachedUiState<List<T>>?> = flatMapLatest { filter ->
    if (isLive(filter)) {
        flow {
            emit(CachedUiState.Loading)
            emit(
                fetch(filter).fold(
                    ifLeft = { CachedUiState.Error(it) },
                    ifRight = { listUiState(it, RefreshStatus.Idle) },
                ),
            )
        }
    } else {
        flowOf(null)
    }
}

/** Search results for the query once typing pauses for [debounceTimeout]; `null` while the query is blank. */
@OptIn(FlowPreview::class)
internal fun <T> Flow<String>.searchResults(
    debounceTimeout: Duration,
    search: suspend (String) -> Either<DataError, List<T>>,
): Flow<CachedUiState<List<T>>?> = debounce(debounceTimeout)
    .distinctUntilChanged()
    .liveResults(isLive = { it.isNotBlank() }, fetch = search)
