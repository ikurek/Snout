package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.core.spec.style.ShouldSpec

class TrackAnalyticsSettingsChangedTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>(autoUnit)

        should("report the Analytics tab settings chosen") {
            TrackAnalyticsSettingsChanged(repository)(AnalyticsPreferences.Default)

            verify { repository.track(TelemetryEvent.AnalyticsSettingsChanged(AnalyticsPreferences.Default)) }
        }
    })
