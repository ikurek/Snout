package com.nightcode.snout.shared.telemetry

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/** [TrackNonFatal]; the [ErrorTracker] behind it comes from `TelemetrySdkModule`, which `SharedModule` includes. */
@Module
@ComponentScan("com.nightcode.snout.shared.telemetry")
class TelemetryModule
