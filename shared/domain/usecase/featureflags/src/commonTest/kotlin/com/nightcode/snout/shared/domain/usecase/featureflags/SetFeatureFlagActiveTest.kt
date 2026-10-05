package com.nightcode.snout.shared.domain.usecase.featureflags

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.TestFeatureFlag
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class SetFeatureFlagActiveTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<FeatureFlagRepository>()
        val subject = SetFeatureFlagActive(GetSelectedProjectId(appStateRepository), repository)

        should("toggle the flag in the selected project") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.setActive(TestSelectedProject.projectId, TestFeatureFlag.id, false) } returns
                Either.Right(TestFeatureFlag.copy(active = false))
            subject(TestFeatureFlag.id, false).shouldBeRight().active shouldBe false
        }

        should("surface forbidden when the key lacks write scope") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.setActive(TestSelectedProject.projectId, TestFeatureFlag.id, true) } returns
                Either.Left(DataError.Forbidden)
            subject(TestFeatureFlag.id, true) shouldBeLeft DataError.Forbidden
        }
    })
