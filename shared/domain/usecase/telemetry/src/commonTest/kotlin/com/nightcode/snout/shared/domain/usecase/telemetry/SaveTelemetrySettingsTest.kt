package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class SaveTelemetrySettingsTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>(autoUnit)

        should("save the settings") {
            val settings = TelemetrySettings(usage = true, errors = false)

            SaveTelemetrySettings(repository)(settings)

            verifySuspend { repository.saveSettings(settings) }
        }
    })
