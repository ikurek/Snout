package com.nightcode.snout.shared.telemetry

/**
 * Turns reporting on and off as the person chose in Settings: usage events (screens and what was used, plus the
 * app's opens and closes) and error reports (non-fatal errors and crashes), each on its own. Both are on until turned
 * off; builds that never report (debug builds, builds without a PostHog key) ignore it.
 */
interface TelemetrySwitch {

    /** Whether this build can report at all; false in debug builds and builds without a PostHog project token. */
    val isAvailable: Boolean

    fun apply(usage: Boolean, errors: Boolean)
}
