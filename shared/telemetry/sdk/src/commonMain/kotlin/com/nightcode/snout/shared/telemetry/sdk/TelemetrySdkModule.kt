package com.nightcode.snout.shared.telemetry.sdk

import com.nightcode.snout.shared.telemetry.TelemetryModule
import com.posthog.kmp.PostHogContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/** PostHog behind the `telemetry` interfaces ([PostHogTelemetry]); `SharedModule` includes it. */
@Module(includes = [TelemetryModule::class, PlatformTelemetryModule::class])
@ComponentScan("com.nightcode.snout.shared.telemetry.sdk")
class TelemetrySdkModule

/**
 * What only the platform knows: PostHog's context (Android's application) and whether this is a debug build. An
 * interface, so the app's graph check has no constructor of PostHog's types to look into.
 */
internal interface TelemetryPlatform {
    val context: PostHogContext
    val isDebugBuild: Boolean
}

@Module
internal expect class PlatformTelemetryModule()
