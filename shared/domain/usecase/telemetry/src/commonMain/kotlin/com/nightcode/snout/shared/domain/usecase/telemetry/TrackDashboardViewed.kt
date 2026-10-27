package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** A dashboard loaded with [tileCount] tiles. Nothing to await: tracking never fails the caller. */
@Single
class TrackDashboardViewed(private val repository: TelemetryRepository) {

    operator fun invoke(tileCount: Int) = repository.track(TelemetryEvent.DashboardViewed(tileCount))
}
