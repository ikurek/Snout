package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveTelemetrySettings(private val repository: TelemetryRepository) {

    operator fun invoke(): Flow<TelemetrySettings> = repository.observeSettings()
}
