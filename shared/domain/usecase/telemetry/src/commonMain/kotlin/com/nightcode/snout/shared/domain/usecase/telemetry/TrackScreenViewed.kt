package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryScreen
import org.koin.core.annotation.Single

/** [screen] came on screen. Nothing to await: tracking never fails the caller. */
@Single
class TrackScreenViewed(private val repository: TelemetryRepository) {

    operator fun invoke(screen: TelemetryScreen) = repository.trackScreen(screen)
}
