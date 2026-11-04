package com.nightcode.snout.shared.domain.usecase.analytics

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPart
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList

@OptIn(TestData::class)
class LoadAnalyticsTest :
    ShouldSpec({
        val range = AnalyticsRange.Last7Days
        val snapshot = TestAnalyticsSnapshot
        val activeUserKeys = setOf(OverviewMetricKey.WeeklyActiveUsers, OverviewMetricKey.MonthlyActiveUsers)
        val overview = snapshot.overview.filter { it.key !in activeUserKeys }
        val activeUsers = snapshot.overview.filter { it.key in activeUserKeys }

        lateinit var repository: AnalyticsRepository

        fun subject(): LoadAnalytics {
            val appStateRepository = mock<AppStateRepository>()
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            repository = mock<AnalyticsRepository>()
            everySuspend { repository.getOverview(any(), any()) } returns Either.Right(overview)
            everySuspend { repository.getActiveUsers(any(), any()) } returns Either.Right(activeUsers)
            everySuspend { repository.getTrends(any(), any()) } returns Either.Right(snapshot.trends)
            everySuspend { repository.getBreakdown(any(), any(), any()) } returns
                Either.Left(DataError.UnexpectedError)
            snapshot.breakdowns.forEach { table ->
                everySuspend { repository.getBreakdown(any(), any(), table.breakdown) } returns Either.Right(table)
            }
            return LoadAnalytics(GetSelectedProjectId(appStateRepository), repository)
        }

        should("start with every shown part pending and end with all of them in") {
            val emissions = subject()(range, AnalyticsPreferences.Default).toList()

            emissions.first().shouldBeRight().pending shouldBe
                AnalyticsPart.shownBy(AnalyticsPreferences.Default).toSet()
            val last = emissions.last().shouldBeRight()
            last.pending.shouldBeEmpty()
            last.overview shouldBe overview + activeUsers
            last.trends shouldBe snapshot.trends
            // Tables PostHog failed are left out; the rest keep display order.
            last.breakdowns shouldBe snapshot.breakdowns.sortedBy { it.breakdown }
        }

        should("emit once per part as it arrives") {
            val preferences = AnalyticsPreferences.Default.copy(breakdowns = setOf(WebBreakdown.Page))

            val emissions = subject()(range, preferences).toList()

            // The empty snapshot, then the overview, active users, trends and the one table.
            emissions.size shouldBe 5
        }

        should("fetch only what the preferences show") {
            val preferences = AnalyticsPreferences.Default.copy(
                metrics = setOf(OverviewMetricKey.Visitors),
                showTrendChart = false,
                breakdowns = setOf(WebBreakdown.Channel),
            )

            subject()(range, preferences).toList()

            verifySuspend(VerifyMode.not) { repository.getActiveUsers(any(), any()) }
            verifySuspend(VerifyMode.not) { repository.getTrends(any(), any()) }
            verifySuspend(VerifyMode.exactly(1)) { repository.getBreakdown(any(), any(), any()) }
        }

        should("fail with the overview's error and end there") {
            val loadAnalytics = subject()
            everySuspend { repository.getOverview(any(), any()) } returns Either.Left(DataError.Forbidden)

            val emissions = loadAnalytics(range, AnalyticsPreferences.Default).toList()

            emissions.last() shouldBeLeft DataError.Forbidden
        }
    })
