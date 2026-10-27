package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** The bottom bar was customised. Nothing to await: tracking never fails the caller. */
@Single
class TrackNavigationChanged(private val repository: TelemetryRepository) {

    operator fun invoke(preferences: NavigationPreferences) = repository.track(
        TelemetryEvent.NavigationChanged(preferences.tabs, preferences.landing),
    )
}
