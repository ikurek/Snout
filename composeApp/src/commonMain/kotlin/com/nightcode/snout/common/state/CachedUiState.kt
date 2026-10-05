package com.nightcode.snout.common.state

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Screen state for cache-first data: cached content renders immediately while a refresh runs. */
internal sealed interface CachedUiState<out T> {
    data object Loading : CachedUiState<Nothing>

    data class Error(val error: DataError) : CachedUiState<Nothing>

    data class Empty(val isRefreshing: Boolean) : CachedUiState<Nothing>

    data class Content<T>(val data: T, val isRefreshing: Boolean, val refreshError: DataError? = null) :
        CachedUiState<T>
}

internal sealed interface RefreshStatus {
    data object Idle : RefreshStatus

    data object Refreshing : RefreshStatus

    data class Failed(val error: DataError) : RefreshStatus
}

internal fun <T> cachedUiState(
    cached: T?,
    refresh: RefreshStatus,
    isEmpty: (T) -> Boolean = {
        false
    },
): CachedUiState<T> = when {
    cached == null && refresh is RefreshStatus.Failed -> CachedUiState.Error(refresh.error)

    cached == null -> CachedUiState.Loading

    isEmpty(cached) -> CachedUiState.Empty(isRefreshing = refresh is RefreshStatus.Refreshing)

    else -> CachedUiState.Content(
        data = cached,
        isRefreshing = refresh is RefreshStatus.Refreshing,
        refreshError = (refresh as? RefreshStatus.Failed)?.error,
    )
}

internal fun <T> listUiState(cached: List<T>?, refresh: RefreshStatus): CachedUiState<List<T>> =
    cachedUiState(cached, refresh) { it.isEmpty() }

/** Tracks a single in-flight refresh; starts as refreshing because screens refresh on open. */
internal class RefreshController {
    private val _status = MutableStateFlow<RefreshStatus>(RefreshStatus.Refreshing)
    val status: StateFlow<RefreshStatus> = _status.asStateFlow()

    suspend fun run(block: suspend () -> Either<DataError, Unit>) {
        _status.value = RefreshStatus.Refreshing
        _status.value = block().fold(
            ifLeft = { RefreshStatus.Failed(it) },
            ifRight = { RefreshStatus.Idle },
        )
    }
}
