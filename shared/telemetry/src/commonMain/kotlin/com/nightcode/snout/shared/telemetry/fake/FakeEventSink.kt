package com.nightcode.snout.shared.telemetry.fake

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.telemetry.EventSink

/** Records what would have been sent, so a test can assert on it. */
@TestData
class FakeEventSink : EventSink {

    data class TrackedEvent(val name: String, val properties: Map<String, Any>)

    val tracked = mutableListOf<TrackedEvent>()
    val screens = mutableListOf<String>()

    override fun track(name: String, properties: Map<String, Any>) {
        tracked += TrackedEvent(name, properties)
    }

    override fun screen(name: String) {
        screens += name
    }
}
