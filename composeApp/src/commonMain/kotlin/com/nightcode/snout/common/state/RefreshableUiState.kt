package com.nightcode.snout.common.state

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Screen state for data that loads again on demand (pull to refresh, another project, a change made in the app): what
 * was loaded stays on screen while the next load runs, and a failed reload shows over it instead of replacing it.
 * One-shot fetches such as detail screens use [LoadState] instead.
 */
internal sealed interface RefreshableUiState<out T> {
    data object Loading : RefreshableUiState<Nothing>

    data class Error(val error: DataError) : RefreshableUiState<Nothing>

    data class Empty(val isRefreshing: Boolean) : RefreshableUiState<Nothing>

    data class Content<T>(val data: T, val isRefreshing: Boolean, val refreshError: DataError? = null) :
        RefreshableUiState<T>
}

internal sealed interface RefreshStatus {
    data object Idle : RefreshStatus

    data object Refreshing : RefreshStatus

    data class Failed(val error: DataError) : RefreshStatus
}

internal fun <T> refreshableUiState(
    loaded: T?,
    refresh: RefreshStatus,
    isEmpty: (T) -> Boolean = {
        false
    },
): RefreshableUiState<T> = when {
    loaded == null && refresh is RefreshStatus.Failed -> RefreshableUiState.Error(refresh.error)

    loaded == null -> RefreshableUiState.Loading

    isEmpty(loaded) -> RefreshableUiState.Empty(isRefreshing = refresh is RefreshStatus.Refreshing)

    else -> RefreshableUiState.Content(
        data = loaded,
        isRefreshing = refresh is RefreshStatus.Refreshing,
        refreshError = (refresh as? RefreshStatus.Failed)?.error,
    )
}

internal fun <T> listUiState(loaded: List<T>?, refresh: RefreshStatus): RefreshableUiState<List<T>> =
    refreshableUiState(loaded, refresh) { it.isEmpty() }

/**
 * Loads with [fetch] for every value this flow emits and again for the latest one on each of [refreshes], cancelling
 * a load still running when the next one starts. The values say what to load (a project's reload, a range, a filter):
 * while loading the same value again the loaded data stays on screen and a failure shows over it; a different value
 * starts from the skeleton, since what is on screen belongs to something else.
 *
 * Runs in [scope] (the view model's) for as long as it lives, so what was loaded is still there when the screen comes
 * back and reloads happen even while it is away.
 */
internal fun <K, T> Flow<K>.loadRefreshable(
    scope: CoroutineScope,
    refreshes: Flow<Unit>,
    isEmpty: (T) -> Boolean = { false },
    fetch: suspend (K) -> Either<DataError, T>,
): StateFlow<RefreshableUiState<T>> =
    refreshableUiStates(refreshes, isEmpty, fetch).stateIn(scope, SharingStarted.Eagerly, RefreshableUiState.Loading)

/** [loadRefreshable] for a list, which shows as empty when it has no items. */
internal fun <K, T> Flow<K>.loadRefreshableList(
    scope: CoroutineScope,
    refreshes: Flow<Unit>,
    fetch: suspend (K) -> Either<DataError, List<T>>,
): StateFlow<RefreshableUiState<List<T>>> = loadRefreshable(scope, refreshes, isEmpty = { it.isEmpty() }, fetch = fetch)

/** The states behind [loadRefreshable]. */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun <K, T> Flow<K>.refreshableUiStates(
    refreshes: Flow<Unit>,
    isEmpty: (T) -> Boolean,
    fetch: suspend (K) -> Either<DataError, T>,
): Flow<RefreshableUiState<T>> = channelFlow {
    var loaded: Loaded<K, T>? = null
    flatMapLatest { key -> refreshes.map { key }.onStart { emit(key) } }.collectLatest { key ->
        val kept = loaded?.takeIf { it.key == key }?.data
        send(refreshableUiState(kept, RefreshStatus.Refreshing, isEmpty))
        fetch(key).fold(
            ifLeft = { error -> send(refreshableUiState(kept, RefreshStatus.Failed(error), isEmpty)) },
            ifRight = { data ->
                loaded = Loaded(key, data)
                send(refreshableUiState(data, RefreshStatus.Idle, isEmpty))
            },
        )
    }
}

private class Loaded<K, T>(val key: K, val data: T)
