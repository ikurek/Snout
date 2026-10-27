package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.InstanceKind
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import org.koin.core.annotation.Single

/**
 * Onboarding finished: a project was picked. The instance is reported only as cloud US, cloud EU or self-hosted,
 * never by its address.
 */
@Single
class TrackOnboarded(private val getInstanceUrl: GetInstanceUrl, private val repository: TelemetryRepository) {

    suspend operator fun invoke() {
        val instanceUrl = getInstanceUrl() ?: return
        repository.track(TelemetryEvent.Onboarded(InstanceKind.of(instanceUrl)))
    }
}
