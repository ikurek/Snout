package com.nightcode.snout.shared.data.testutils

import androidx.datastore.preferences.core.emptyPreferences
import com.nightcode.snout.shared.storage.datastore.TelemetryDataStore

/** A [TelemetryDataStore] over an empty in-memory store: nothing switched yet. */
fun inMemoryTelemetryDataStore(): TelemetryDataStore = TelemetryDataStore(InMemoryDataStore(emptyPreferences()))
