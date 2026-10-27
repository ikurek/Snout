package com.nightcode.snout.shared.telemetry.sdk

import com.posthog.kmp.PostHogContext
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@Module
internal actual class PlatformTelemetryModule {
    /** Xcode's Debug configuration links the debug framework; Release, which archives use, the release one. */
    @OptIn(ExperimentalNativeApi::class)
    @Single
    fun telemetryPlatform(): TelemetryPlatform = object : TelemetryPlatform {
        override val context = PostHogContext()
        override val isDebugBuild = Platform.isDebugBinary
    }
}
