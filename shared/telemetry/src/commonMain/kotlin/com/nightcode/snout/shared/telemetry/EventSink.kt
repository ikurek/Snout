package com.nightcode.snout.shared.telemetry

/**
 * Where usage events go: PostHog (`telemetry/sdk`). The data layer hands it wire names and properties; the domain's
 * `TelemetryEvent`s never reach here.
 */
interface EventSink {

    fun track(name: String, properties: Map<String, Any>)

    /** A screen was shown; [name] is the screen's kind, never anything that identifies what it shows. */
    fun screen(name: String)
}
