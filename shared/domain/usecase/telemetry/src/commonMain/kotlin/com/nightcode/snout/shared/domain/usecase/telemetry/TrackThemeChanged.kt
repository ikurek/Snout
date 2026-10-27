package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** The theme was set to [themeMode]. Nothing to await: tracking never fails the caller. */
@Single
class TrackThemeChanged(private val repository: TelemetryRepository) {

    operator fun invoke(themeMode: ThemeMode) = repository.track(TelemetryEvent.ThemeChanged(themeMode))
}
