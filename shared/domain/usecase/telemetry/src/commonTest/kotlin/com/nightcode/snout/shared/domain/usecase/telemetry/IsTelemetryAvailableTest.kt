package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class IsTelemetryAvailableTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>()

        should("tell whether this build can report") {
            every { repository.isAvailable() } returns false

            IsTelemetryAvailable(repository)() shouldBe false
        }
    })
