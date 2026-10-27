package com.nightcode.snout.shared.data.telemetry

import com.nightcode.snout.shared.data.telemetry.mapper.toName
import com.nightcode.snout.shared.data.telemetry.mapper.toProperties
import com.nightcode.snout.shared.data.telemetry.mapper.toTelemetrySettings
import com.nightcode.snout.shared.data.telemetry.mapper.wireName
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryScreen
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import com.nightcode.snout.shared.storage.datastore.TelemetryDataStore
import com.nightcode.snout.shared.telemetry.EventSink
import com.nightcode.snout.shared.telemetry.TelemetrySwitch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [TelemetryRepository::class])
internal class TelemetryRepositoryImpl(
    private val eventSink: EventSink,
    private val telemetrySwitch: TelemetrySwitch,
    private val dataStore: TelemetryDataStore,
) : TelemetryRepository {

    override fun track(event: TelemetryEvent) = eventSink.track(event.toName(), event.toProperties())

    override fun trackScreen(screen: TelemetryScreen) = eventSink.screen(screen.wireName())

    override fun isAvailable(): Boolean = telemetrySwitch.isAvailable

    override fun observeSettings(): Flow<TelemetrySettings> = dataStore.observeSettings().map {
        it.toTelemetrySettings()
    }

    override suspend fun saveSettings(settings: TelemetrySettings) {
        dataStore.saveSettings(usage = settings.usage, errors = settings.errors)
        telemetrySwitch.apply(usage = settings.usage, errors = settings.errors)
    }

    override suspend fun applyStoredSettings() {
        val settings = observeSettings().first()
        telemetrySwitch.apply(usage = settings.usage, errors = settings.errors)
    }
}
