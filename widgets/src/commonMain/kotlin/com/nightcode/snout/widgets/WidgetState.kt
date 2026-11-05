package com.nightcode.snout.widgets

import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import kotlin.coroutines.cancellation.CancellationException

/**
 * What a widget draws after a refresh. Nothing is kept between refreshes: a refresh that fails shows [Failed], never
 * what an earlier one loaded.
 */
sealed interface WidgetState<out T : Any> {
    /** No key: the app has to be opened and connected first. */
    data object SignedOut : WidgetState<Nothing>

    data class Failed(val error: DataError) : WidgetState<Nothing>

    data class Loaded<T : Any>(val content: T) : WidgetState<T>
}

/**
 * Runs [load] once the app is signed in, or says it isn't. Each widget names its own project, so the app needn't have
 * one selected. Never throws: the widget extension has no screen to recover on, so a failure below the use cases
 * (the Keychain before the first unlock) becomes [DataError.UnexpectedError] as well.
 */
@Suppress("TooGenericExceptionCaught") // any failure leaves the widget with the same "couldn't load" state
internal suspend fun <T : Any> IsSignedIn.loadWhenSignedIn(
    load: suspend () -> WidgetState<T>,
): WidgetState<T> = try {
    if (invoke()) load() else WidgetState.SignedOut
} catch (exception: CancellationException) {
    throw exception
} catch (_: Exception) {
    WidgetState.Failed(DataError.UnexpectedError)
}
