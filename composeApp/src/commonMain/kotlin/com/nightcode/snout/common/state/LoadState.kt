package com.nightcode.snout.common.state

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError

/**
 * Screen state for a one-shot fetch with no cache behind it, such as a detail screen. Cache-observing screens use
 * [CachedUiState] instead.
 *
 * Only the fetch itself fails into [Error]. A failed action on loaded data (a toggle, a status change) is a one-shot
 * side effect shown as a snackbar, unless it must stay on screen next to what failed, as in a sheet or under the
 * control; only then does the screen state keep a status for it.
 */
internal sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>

    data class Error(val error: DataError) : LoadState<Nothing>

    data class Loaded<T>(val data: T) : LoadState<T>
}

internal fun <T> Either<DataError, T>.toLoadState(): LoadState<T> = fold(
    ifLeft = { LoadState.Error(it) },
    ifRight = { LoadState.Loaded(it) },
)

internal val <T> LoadState<T>.dataOrNull: T? get() = (this as? LoadState.Loaded)?.data
