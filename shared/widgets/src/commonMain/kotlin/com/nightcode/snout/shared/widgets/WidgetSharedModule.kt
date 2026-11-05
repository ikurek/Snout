package com.nightcode.snout.shared.widgets

import com.nightcode.snout.shared.core.CoreModule
import com.nightcode.snout.shared.telemetry.ErrorTracker
import com.nightcode.snout.shared.telemetry.EventSink
import com.nightcode.snout.shared.telemetry.TelemetryModule
import com.nightcode.snout.shared.telemetry.TelemetrySwitch
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.plugin.module.dsl.koinConfiguration

/**
 * Composition root of the shared layer for the iOS widget extension: the whole [CoreModule], with telemetry
 * switched off. The extension never links PostHog's SDK; the app reports on its own. On Android, widgets run in the
 * app's process and use the app's graph (`SharedModule`).
 */
// TelemetryModule (TrackNonFatal) is listed here: the graph check doesn't follow it through SilentTelemetryModule.
@Module(includes = [CoreModule::class, TelemetryModule::class, SilentTelemetryModule::class])
class WidgetSharedModule

@Module
@ComponentScan("com.nightcode.snout.shared.widgets")
internal class SilentTelemetryModule

/** Reports nothing: the widget extension sends no usage or error reports. */
@Single(binds = [EventSink::class, ErrorTracker::class, TelemetrySwitch::class])
internal class SilentTelemetry :
    EventSink,
    ErrorTracker,
    TelemetrySwitch {

    override val isAvailable: Boolean = false

    override fun apply(usage: Boolean, errors: Boolean) = Unit

    override fun track(name: String, properties: Map<String, Any>) = Unit

    override fun screen(name: String) = Unit

    override fun trackNonFatal(throwable: Throwable, message: String) = Unit
}

/** Koin entry point for the compiler plugin's build-time check of [WidgetSharedModule]'s graph (see `SharedGraph`). */
@KoinApplication(modules = [WidgetSharedModule::class])
internal object WidgetGraph

/** Never called: the plugin only checks graphs at a call site such as this one. */
@Suppress("unused")
internal fun widgetGraphConfiguration() = koinConfiguration<WidgetGraph>()
