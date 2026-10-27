package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.core.spec.style.ShouldSpec

class TrackIssueStatusChangedTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>(autoUnit)

        should("report the status an issue moved to") {
            TrackIssueStatusChanged(repository)(IssueStatus.Resolved)

            verify { repository.track(TelemetryEvent.IssueStatusChanged(IssueStatus.Resolved)) }
        }
    })
