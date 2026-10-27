package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.core.spec.style.ShouldSpec

class TrackInsightViewedTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>(autoUnit)

        should("report an insight by its kind") {
            TrackInsightViewed(repository)(InsightKind.Funnels)

            verify { repository.track(TelemetryEvent.InsightViewed(InsightKind.Funnels)) }
        }
    })
