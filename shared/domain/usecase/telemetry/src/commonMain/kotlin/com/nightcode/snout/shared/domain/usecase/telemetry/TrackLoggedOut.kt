package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** The person logged out. Nothing to await: tracking never fails the caller. */
@Single
class TrackLoggedOut(private val repository: TelemetryRepository) {

    operator fun invoke() = repository.track(TelemetryEvent.LoggedOut)
}
