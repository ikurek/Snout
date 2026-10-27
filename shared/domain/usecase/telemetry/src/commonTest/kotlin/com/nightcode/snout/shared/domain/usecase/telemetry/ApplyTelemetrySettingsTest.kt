package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class ApplyTelemetrySettingsTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>(autoUnit)

        should("apply the stored settings") {
            ApplyTelemetrySettings(repository)()

            verifySuspend { repository.applyStoredSettings() }
        }
    })
