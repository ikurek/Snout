package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import org.koin.core.annotation.Single

/** Stores what the person lets Snout report and applies it at once. */
@Single
class SaveTelemetrySettings(private val repository: TelemetryRepository) {

    suspend operator fun invoke(settings: TelemetrySettings) = repository.saveSettings(settings)
}
