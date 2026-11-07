package com.nightcode.snout.widgets

import arrow.core.Either
import com.nightcode.snout.shared.common.format.ChangeTone
import com.nightcode.snout.shared.common.format.TrendChange
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.analytics.HighlightsRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.analytics.LoadProjectHighlights
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.links.GetPostHogUrl
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class HighlightsWidgetLoaderTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>()
        val appStateRepository = mock<AppStateRepository>()
        val analyticsRepository = mock<AnalyticsRepository>()
        val highlightsRepository = mock<HighlightsRepository>()

        fun subject(key: PersonalApiKey? = TestPersonalApiKey) = run {
            everySuspend { authRepository.getPersonalApiKey() } returns key
            everySuspend { authRepository.getInstanceUrl() } returns "https://eu.posthog.com"
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            HighlightsWidgetLoader(
                isSignedIn = IsSignedIn(authRepository),
                loadProjectHighlights = LoadProjectHighlights(analyticsRepository, highlightsRepository),
                getPostHogUrl = GetPostHogUrl(authRepository, appStateRepository),
            )
        }

        val projectId = 7L
        val config = HighlightsWidgetConfig(
            projectId = projectId,
            range = AnalyticsRange.Last7Days,
            metrics = listOf(
                HighlightMetric.Exceptions,
                HighlightMetric.Mrr,
                HighlightMetric.LatestAppVersion,
                HighlightMetric.Visitors,
            ),
        )

        beforeEach {
            everySuspend { analyticsRepository.getOverview(any(), any()) } returns
                Either.Right(TestAnalyticsSnapshot.overview)
            everySuspend { highlightsRepository.getExceptions(any(), any()) } returns Either.Right(
                mapOf(
                    HighlightMetric.Exceptions to
                        Highlight.Figure(206.0, OverviewMetricKind.Count, -10.0, isIncreaseBad = true),
                ),
            )
            everySuspend { highlightsRepository.getRevenue(any()) } returns
                Either.Right(mapOf(HighlightMetric.Mrr to Highlight.Money(12_430.0, "USD")))
            everySuspend { highlightsRepository.getLatestAppVersion(any()) } returns Either.Right(Highlight.None)
        }

        should("show a tile per metric in the widget's order, saying which PostHog doesn't collect") {
            subject().load(config) shouldBe WidgetState.Loaded(
                HighlightsWidgetContent(
                    range = AnalyticsRange.Last7Days,
                    tiles = listOf(
                        // Fewer exceptions is good news.
                        HighlightTile(HighlightMetric.Exceptions, "206", TrendChange("↓ 10%", ChangeTone.Good)),
                        HighlightTile(HighlightMetric.Mrr, "$12.4k", change = null),
                        HighlightTile(HighlightMetric.LatestAppVersion, null, null, HighlightMissing.NotSetUp),
                        HighlightTile(HighlightMetric.Visitors, "12.4k", TrendChange("↑ 12.8%", ChangeTone.Good)),
                    ),
                    link = "https://eu.posthog.com/project/7/web",
                ),
            )
            verifySuspend(not) { highlightsRepository.getActiveUsers(any()) }
        }

        should("mark the tiles whose request failed, keeping the others") {
            everySuspend { highlightsRepository.getRevenue(any()) } returns Either.Left(DataError.Forbidden)
            val state = subject().load(config) as WidgetState.Loaded
            state.content.tiles[1] shouldBe HighlightTile(HighlightMetric.Mrr, null, null, HighlightMissing.Failed)
            state.content.tiles[0].value shouldBe "206"
        }

        should("say there are no subscriptions when PostHog has revenue but none of it recurs") {
            everySuspend { highlightsRepository.getRevenue(any()) } returns
                Either.Right(mapOf(HighlightMetric.Mrr to Highlight.NoSubscriptions))
            val state = subject().load(config) as WidgetState.Loaded
            state.content.tiles[1] shouldBe
                HighlightTile(HighlightMetric.Mrr, null, null, HighlightMissing.NoSubscriptions)
        }

        should("show the error once when every request failed") {
            val offline = Either.Left(DataError.ConnectionError)
            everySuspend { analyticsRepository.getOverview(any(), any()) } returns offline
            everySuspend { highlightsRepository.getExceptions(any(), any()) } returns offline
            everySuspend { highlightsRepository.getRevenue(any()) } returns offline
            everySuspend { highlightsRepository.getLatestAppVersion(any()) } returns offline
            subject().load(config) shouldBe WidgetState.Failed(DataError.ConnectionError)
        }

        should("ask to connect, without querying, when signed out") {
            subject(key = null).load(config) shouldBe WidgetState.SignedOut
            verifySuspend(not) { analyticsRepository.getOverview(any(), any()) }
        }
    })
