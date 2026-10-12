package com.nightcode.snout.shared.domain.usecase.analytics

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec

@OptIn(TestData::class)
class GetAnalyticsTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<AnalyticsRepository>()
        val subject = GetAnalytics(GetSelectedProjectId(appStateRepository), repository)

        should("get the snapshot of the selected project") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            val preferences = AnalyticsPreferences.Default
            everySuspend {
                repository.getSnapshot(TestSelectedProject.projectId, AnalyticsRange.Last7Days, preferences)
            } returns Either.Right(TestAnalyticsSnapshot)
            subject(AnalyticsRange.Last7Days, preferences) shouldBeRight TestAnalyticsSnapshot
            verifySuspend {
                repository.getSnapshot(TestSelectedProject.projectId, AnalyticsRange.Last7Days, preferences)
            }
        }
    })
