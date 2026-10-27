package com.nightcode.snout.shared.telemetry.fake

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.telemetry.ErrorTracker

/** Records what would have been reported, so a test can assert on it or on its absence. */
@TestData
class FakeErrorTracker : ErrorTracker {

    data class TrackedError(val throwable: Throwable, val message: String)

    val tracked = mutableListOf<TrackedError>()

    override fun trackNonFatal(throwable: Throwable, message: String) {
        tracked += TrackedError(throwable, message)
    }
}
