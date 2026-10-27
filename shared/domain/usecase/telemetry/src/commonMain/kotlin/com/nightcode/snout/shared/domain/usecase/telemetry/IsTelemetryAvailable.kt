package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import org.koin.core.annotation.Single

/** Whether this build can report at all, so Settings only offers switches that do something. */
@Single
class IsTelemetryAvailable(private val repository: TelemetryRepository) {

    operator fun invoke(): Boolean = repository.isAvailable()
}
