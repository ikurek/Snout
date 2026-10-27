package com.nightcode.snout.shared.telemetry.sdk

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import com.posthog.kmp.PostHogContext
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class PlatformTelemetryModule {
    /** Debug variants are debuggable and release ones aren't; this module has no `BuildConfig` of the app to read. */
    @Single
    fun telemetryPlatform(context: Context): TelemetryPlatform = object : TelemetryPlatform {
        override val context = PostHogContext(context.applicationContext as Application)
        override val isDebugBuild = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
}
