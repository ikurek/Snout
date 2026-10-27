package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** An annotation was added, and PostHog took it. Nothing to await: tracking never fails the caller. */
@Single
class TrackAnnotationCreated(private val repository: TelemetryRepository) {

    operator fun invoke() = repository.track(TelemetryEvent.AnnotationCreated)
}
