package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/**
 * An error tracking issue moved to [status], and PostHog took the change. Nothing to await: tracking never fails
 * the caller.
 */
@Single
class TrackIssueStatusChanged(private val repository: TelemetryRepository) {

    operator fun invoke(status: IssueStatus) = repository.track(TelemetryEvent.IssueStatusChanged(status))
}
