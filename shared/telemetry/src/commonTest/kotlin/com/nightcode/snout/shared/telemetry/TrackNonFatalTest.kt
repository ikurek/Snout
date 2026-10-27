package com.nightcode.snout.shared.telemetry

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.telemetry.fake.FakeErrorTracker
import com.nightcode.snout.shared.telemetry.fake.FakeErrorTracker.TrackedError
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.coroutines.cancellation.CancellationException

@OptIn(TestData::class)
class TrackNonFatalTest :
    ShouldSpec({
        should("report a failure with what the app was doing") {
            val errorTracker = FakeErrorTracker()
            val failure = IllegalStateException("boom")

            TrackNonFatal(errorTracker)(failure, "GET /api/projects/:id/feature_flags/")

            errorTracker.tracked shouldContainExactly listOf(
                TrackedError(failure, "GET /api/projects/:id/feature_flags/"),
            )
        }

        should("not report cancellation") {
            val errorTracker = FakeErrorTracker()

            TrackNonFatal(errorTracker)(CancellationException("left the screen"), "GET /api/users/@me/")

            errorTracker.tracked.shouldBeEmpty()
        }
    })
