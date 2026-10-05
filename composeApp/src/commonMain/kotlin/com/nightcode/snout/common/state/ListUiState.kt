package com.nightcode.snout.common.state

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Screen state for a cache-first list: cached items render immediately while a refresh runs. */
internal sealed interface ListUiState<out T> {
    data object Loading : ListUiState<Nothing>

    data class Error(val error: DataError) : ListUiState<Nothing>

    data class Empty(val isRefreshing: Boolean) : ListUiState<Nothing>

    data class Content<T>(val items: List<T>, val isRefreshing: Boolean, val refreshError: DataError? = null) :
        ListUiState<T>
}

internal sealed interface RefreshStatus {
    data object Idle : RefreshStatus

    data object Refreshing : RefreshStatus

    data class Failed(val error: DataError) : RefreshStatus
}

internal fun <T> listUiState(cached: List<T>?, refresh: RefreshStatus): ListUiState<T> = when {
    cached == null && refresh is RefreshStatus.Failed -> ListUiState.Error(refresh.error)

    cached == null -> ListUiState.Loading

    cached.isEmpty() -> ListUiState.Empty(isRefreshing = refresh is RefreshStatus.Refreshing)

    else -> ListUiState.Content(
        items = cached,
        isRefreshing = refresh is RefreshStatus.Refreshing,
        refreshError = (refresh as? RefreshStatus.Failed)?.error,
    )
}

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
