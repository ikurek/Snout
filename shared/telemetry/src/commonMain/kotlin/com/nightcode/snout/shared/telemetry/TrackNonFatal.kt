package com.nightcode.snout.shared.telemetry

import org.koin.core.annotation.Single
import kotlin.coroutines.cancellation.CancellationException

/**
 * Reports a failure that is a bug, from the choke point that turns it into an error value (the API client). The one
 * place every report goes through, so it is also where coroutine cancellation is dropped: a screen leaving
 * mid-request is not a bug, and a call site that forgets to rethrow it must not bury real errors.
 */
@Single
class TrackNonFatal(private val errorTracker: ErrorTracker) {

    operator fun invoke(throwable: Throwable, message: String) {
        if (throwable is CancellationException) return
        errorTracker.trackNonFatal(throwable, message)
    }
}
