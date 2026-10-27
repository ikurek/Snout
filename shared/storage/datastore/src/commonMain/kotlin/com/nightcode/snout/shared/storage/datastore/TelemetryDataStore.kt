package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

private val TelemetryUsageKey = booleanPreferencesKey("telemetry_usage")
private val TelemetryErrorsKey = booleanPreferencesKey("telemetry_errors")

/** Raw telemetry switches as stored; `null` means never switched, which the data layer reads as on. */
data class StoredTelemetrySettings(val usage: Boolean?, val errors: Boolean?)

/**
 * Kept apart from [AppStateDataStore]: logging out leaves it alone, since it's the person's choice, not account
 * data.
 */
@Single
class TelemetryDataStore(private val dataStore: DataStore<Preferences>) {

    fun observeSettings(): Flow<StoredTelemetrySettings> = dataStore.data.map {
        StoredTelemetrySettings(usage = it[TelemetryUsageKey], errors = it[TelemetryErrorsKey])
    }

    suspend fun saveSettings(usage: Boolean, errors: Boolean) {
        dataStore.edit {
            it[TelemetryUsageKey] = usage
            it[TelemetryErrorsKey] = errors
        }
    }
}
