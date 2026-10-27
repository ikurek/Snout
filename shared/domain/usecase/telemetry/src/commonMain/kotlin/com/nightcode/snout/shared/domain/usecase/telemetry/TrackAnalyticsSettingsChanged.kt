package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** What the Analytics tab shows was changed. Nothing to await: tracking never fails the caller. */
@Single
class TrackAnalyticsSettingsChanged(private val repository: TelemetryRepository) {

    operator fun invoke(preferences: AnalyticsPreferences) = repository.track(
        TelemetryEvent.AnalyticsSettingsChanged(preferences),
    )
}
