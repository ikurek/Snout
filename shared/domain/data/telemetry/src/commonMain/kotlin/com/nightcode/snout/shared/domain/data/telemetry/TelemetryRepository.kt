package com.nightcode.snout.shared.domain.data.telemetry

import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryScreen
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import kotlinx.coroutines.flow.Flow

/**
 * Reports how the app is used, as the person allows. Tracking has nothing to await and nothing a caller could do
 * about a failure, so it returns nothing.
 */
interface TelemetryRepository {

    fun track(event: TelemetryEvent)

    fun trackScreen(screen: TelemetryScreen)

    /** Whether this build can report at all: debug builds and builds without Snout's PostHog key never do. */
    fun isAvailable(): Boolean

    fun observeSettings(): Flow<TelemetrySettings>

    /** Stores [settings] and applies them at once. */
    suspend fun saveSettings(settings: TelemetrySettings)

    /** Applies the stored settings; once at launch, before anything is tracked. */
    suspend fun applyStoredSettings()
}
