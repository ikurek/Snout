package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class DetermineInitialLocationTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>(autoUnit)
        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val subject = DetermineInitialLocation(authRepository, appStateRepository)

        should("return Login when no api key is saved") {
            everySuspend { authRepository.getPersonalApiKey() } returns null
            subject() shouldBe InitialLocation.Login
        }

        should("return ProjectSelection when api key saved but no project selected") {
            everySuspend { authRepository.getPersonalApiKey() } returns TestPersonalApiKey
            everySuspend { appStateRepository.getSelectedProject() } returns null
            subject() shouldBe InitialLocation.ProjectSelection
        }

        should("return Home when api key saved and project selected") {
            everySuspend { authRepository.getPersonalApiKey() } returns TestPersonalApiKey
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            subject() shouldBe InitialLocation.Home
        }
    })
