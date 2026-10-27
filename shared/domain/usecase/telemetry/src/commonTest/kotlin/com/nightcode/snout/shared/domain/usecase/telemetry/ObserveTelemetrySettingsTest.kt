package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class ObserveTelemetrySettingsTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>()

        should("observe what the person lets Snout report") {
            val settings = TelemetrySettings(usage = false, errors = true)
            every { repository.observeSettings() } returns flowOf(settings)

            ObserveTelemetrySettings(repository)().first() shouldBe settings
        }
    })
