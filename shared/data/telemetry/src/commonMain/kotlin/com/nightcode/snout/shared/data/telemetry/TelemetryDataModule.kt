package com.nightcode.snout.shared.data.telemetry

import com.nightcode.snout.shared.storage.datastore.DataStoreModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/** The sink and switch are the SDK's (`TelemetrySdkModule`), which `SharedModule` includes next to this. */
@Module(includes = [DataStoreModule::class])
@ComponentScan("com.nightcode.snout.shared.data.telemetry")
class TelemetryDataModule
