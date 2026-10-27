package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** An insight loaded. Nothing to await: tracking never fails the caller. */
@Single
class TrackInsightViewed(private val repository: TelemetryRepository) {

    operator fun invoke(kind: InsightKind) = repository.track(TelemetryEvent.InsightViewed(kind))
}
