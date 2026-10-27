package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import org.koin.core.annotation.Single

/** Starts reporting as the person last chose; the platform calls it once at launch, before anything is tracked. */
@Single
class ApplyTelemetrySettings(private val repository: TelemetryRepository) {

    suspend operator fun invoke() = repository.applyStoredSettings()
}
