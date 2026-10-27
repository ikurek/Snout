package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.ProjectSelectionSource
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** A project was picked from [source]. Nothing to await: tracking never fails the caller. */
@Single
class TrackProjectSelected(private val repository: TelemetryRepository) {

    operator fun invoke(source: ProjectSelectionSource) = repository.track(TelemetryEvent.ProjectSelected(source))
}
