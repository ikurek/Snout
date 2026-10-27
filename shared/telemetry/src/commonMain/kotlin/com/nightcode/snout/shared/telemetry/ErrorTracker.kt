package com.nightcode.snout.shared.telemetry

/**
 * Where the failures that are bugs go: PostHog (`telemetry/sdk`; this module stays free of SDKs, so the layers that
 * report can depend on it). Shared code reports through [TrackNonFatal], which drops cancellation, at the choke
 * points that turn a throwable into an error value, so a screen showing "Something went wrong" is matched by a report.
 */
interface ErrorTracker {

    /** Records [throwable] as a non-fatal error, with [message] saying what the app was doing. */
    fun trackNonFatal(throwable: Throwable, message: String)
}
