package com.nightcode.snout.shared.api

import com.nightcode.snout.shared.telemetry.TrackNonFatal
import kotlin.concurrent.Volatile

/** Where [networkCall] reports unexpected failures; set by [ApiModule] when it builds the client. */
internal object FailureReport {
    @Volatile
    var trackNonFatal: TrackNonFatal? = null
}
