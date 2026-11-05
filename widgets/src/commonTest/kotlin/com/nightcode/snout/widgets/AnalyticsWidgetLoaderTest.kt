package com.nightcode.snout.widgets

import arrow.core.Either
import com.nightcode.snout.shared.common.format.ChangeTone
import com.nightcode.snout.shared.common.format.TrendChange
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.analytics.LoadAnalytics
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.links.GetPostHogUrl
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class AnalyticsWidgetLoaderTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>()
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<AnalyticsRepository>()

        fun subject(key: PersonalApiKey? = TestPersonalApiKey, project: SelectedProject? = TestSelectedProject) = run {
            everySuspend { authRepository.getPersonalApiKey() } returns key
            everySuspend { authRepository.getInstanceUrl() } returns "https://eu.posthog.com"
            everySuspend { appStateRepository.getSelectedProject() } returns project
            AnalyticsWidgetLoader(
                isSignedIn = IsSignedIn(authRepository),
                loadAnalytics = LoadAnalytics(GetSelectedProjectId(appStateRepository), repository),
                getPostHogUrl = GetPostHogUrl(authRepository, appStateRepository),
            )
        }

        /** Another project than the app has selected: each widget names its own. */
        val projectId = 7L
        val views = AnalyticsWidgetConfig(projectId, OverviewMetricKey.Views, AnalyticsRange.Today)

        beforeEach {
            everySuspend { repository.getOverview(any(), any()) } returns Either.Right(TestAnalyticsSnapshot.overview)
            everySuspend { repository.getTrends(any(), any()) } returns Either.Right(TestAnalyticsSnapshot.trends)
        }

        should("lead with the widget's stat and its line, for the widget's range") {
            val state = subject().load(views)
            state shouldBe WidgetState.Loaded(
                AnalyticsWidgetContent(
                    range = AnalyticsRange.Today,
                    stats = listOf(
                        WidgetStat(OverviewMetricKey.Views, "48.2k", TrendChange("↓ 5.5%", ChangeTone.Bad)),
                        WidgetStat(OverviewMetricKey.Visitors, "12.4k", TrendChange("↑ 12.8%", ChangeTone.Good)),
                        WidgetStat(OverviewMetricKey.Sessions, "15k", TrendChange("↑ 0.1%", ChangeTone.Good)),
                        WidgetStat(
                            OverviewMetricKey.SessionDuration,
                            "2m 23s",
                            TrendChange("↑ 19.2%", ChangeTone.Good),
                        ),
                        // Bounce rate going up is bad news.
                        WidgetStat(OverviewMetricKey.BounceRate, "42%", TrendChange("↑ 7.7%", ChangeTone.Bad)),
                    ),
                    series = listOf(6_100.0, 6_900.0, 6_400.0, 7_700.0, 4_900.0, 7_300.0, 8_900.0),
                    link = "https://eu.posthog.com/project/7/web",
                ),
            )
            // Only what the widget shows is queried: no active users, no breakdown tables.
            verifySuspend { repository.getOverview(projectId, AnalyticsRange.Today) }
            verifySuspend { repository.getTrends(projectId, AnalyticsRange.Today) }
            verifySuspend(not) { repository.getActiveUsers(any(), any()) }
            verifySuspend(not) { repository.getBreakdown(any(), any(), any()) }
        }

        should("not query trends for a stat PostHog has no trend of") {
            val bounceRate = AnalyticsWidgetConfig(projectId, OverviewMetricKey.BounceRate, AnalyticsRange.Last7Days)
            val state = subject().load(bounceRate) as WidgetState.Loaded
            state.content.stats.first().key shouldBe OverviewMetricKey.BounceRate
            state.content.series shouldBe emptyList()
            verifySuspend(not) { repository.getTrends(any(), any()) }
        }

        should("show the error of a failed refresh") {
            everySuspend { repository.getOverview(any(), any()) } returns Either.Left(DataError.RateLimited)
            subject().load(views) shouldBe WidgetState.Failed(DataError.RateLimited)
        }

        should("ask to connect, without querying, when signed out") {
            subject(key = null).load(views) shouldBe WidgetState.SignedOut
            verifySuspend(not) { repository.getOverview(any(), any()) }
        }

        should("load its own project even before the app has one selected") {
            subject(project = null).load(views) shouldBe (subject().load(views))
        }

        should("turn a failure below the use cases into an error instead of throwing") {
            val loader = subject()
            everySuspend { authRepository.getPersonalApiKey() } throws IllegalStateException("Keychain locked")
            loader.load(views) shouldBe WidgetState.Failed(DataError.UnexpectedError)
        }
    })
