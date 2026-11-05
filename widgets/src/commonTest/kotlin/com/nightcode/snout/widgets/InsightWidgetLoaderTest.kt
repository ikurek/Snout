package com.nightcode.snout.widgets

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.TestInsightDetail
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.insights.GetInsightDetail
import com.nightcode.snout.shared.domain.usecase.insights.GetInsights
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.shared.domain.usecase.links.GetPostHogUrl
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class InsightWidgetLoaderTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>()
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<InsightRepository>()

        val subject = run {
            val getSelectedProjectId = GetSelectedProjectId(appStateRepository)
            InsightWidgetLoader(
                isSignedIn = IsSignedIn(authRepository),
                getInsightDetail = GetInsightDetail(getSelectedProjectId, repository),
                getInsights = GetInsights(getSelectedProjectId, repository),
                searchInsights = SearchInsights(getSelectedProjectId, repository),
                getPostHogUrl = GetPostHogUrl(authRepository, appStateRepository),
            )
        }

        beforeEach {
            everySuspend { authRepository.getPersonalApiKey() } returns TestPersonalApiKey
            everySuspend { authRepository.getInstanceUrl() } returns "https://us.posthog.com"
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.getInsight(any(), any()) } returns Either.Right(TestInsightDetail)
        }

        should("load the insight from the widget's project, whichever the app has selected") {
            val state = subject.load(projectId = 7L, insightId = TestInsightSummary.id) as WidgetState.Loaded
            state.content.name shouldBe TestInsightSummary.name
            state.content.link shouldBe "https://us.posthog.com/project/7/insights/abc123"
            verifySuspend { repository.getInsight(7L, TestInsightSummary.id) }
        }

        should("offer only the insights a widget can show, from the widget's project") {
            val retention = TestInsightSummary.copy(id = 11, name = "Retention", kind = InsightKind.Retention)
            everySuspend { repository.getInsights(7L) } returns Either.Right(listOf(TestInsightSummary, retention))
            subject.choices(projectId = 7L, query = "") shouldBe WidgetState.Loaded(
                listOf(InsightChoice(TestInsightSummary.id, TestInsightSummary.name)),
            )
        }
    })
