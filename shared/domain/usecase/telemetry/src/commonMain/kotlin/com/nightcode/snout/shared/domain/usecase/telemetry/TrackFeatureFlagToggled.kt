package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/**
 * A feature flag was switched [active] or off, and PostHog took the change. Nothing to await: tracking never fails
 * the caller.
 */
@Single
class TrackFeatureFlagToggled(private val repository: TelemetryRepository) {

    operator fun invoke(active: Boolean) = repository.track(TelemetryEvent.FeatureFlagToggled(active))
}
