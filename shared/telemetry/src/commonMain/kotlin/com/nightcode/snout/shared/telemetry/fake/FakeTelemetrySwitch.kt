package com.nightcode.snout.shared.telemetry.fake

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.telemetry.TelemetrySwitch

/** Records each switch position applied, in order. */
@TestData
class FakeTelemetrySwitch(override val isAvailable: Boolean = true) : TelemetrySwitch {

    data class Applied(val usage: Boolean, val errors: Boolean)

    val applied = mutableListOf<Applied>()

    override fun apply(usage: Boolean, errors: Boolean) {
        applied += Applied(usage, errors)
    }
}
