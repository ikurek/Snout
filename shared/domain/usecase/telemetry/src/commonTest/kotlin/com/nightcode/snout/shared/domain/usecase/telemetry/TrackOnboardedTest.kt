package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.telemetry.InstanceKind
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.kotest.core.spec.style.ShouldSpec

class TrackOnboardedTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>(autoUnit)
        val authRepository = mock<AuthRepository>()

        should("report the instance only as its kind") {
            everySuspend { authRepository.getInstanceUrl() } returns "https://posthog.example.com"

            TrackOnboarded(GetInstanceUrl(authRepository), repository)()

            verify { repository.track(TelemetryEvent.Onboarded(InstanceKind.SelfHosted)) }
        }

        should("report nothing without an instance") {
            everySuspend { authRepository.getInstanceUrl() } returns null

            TrackOnboarded(GetInstanceUrl(authRepository), repository)()

            verify(VerifyMode.not) { repository.track(TelemetryEvent.Onboarded(InstanceKind.SelfHosted)) }
        }
    })
