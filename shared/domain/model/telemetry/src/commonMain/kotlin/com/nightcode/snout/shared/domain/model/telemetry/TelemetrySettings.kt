package com.nightcode.snout.shared.domain.model.telemetry

/**
 * What the person lets Snout report to its own PostHog project, each on its own: [usage] (screens, what was used, the
 * app's opens and closes) and [errors] (error reports and crashes). Both are on until switched off in Settings.
 */
data class TelemetrySettings(val usage: Boolean, val errors: Boolean) {
    companion object {
        val Default = TelemetrySettings(usage = true, errors = true)
    }
}
