package com.nightcode.snout.shared.domain.usecase.analytics

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.analytics.HighlightsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.common.DataError
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class LoadProjectHighlightsTest :
    ShouldSpec({
        val projectId = 7L
        val range = AnalyticsRange.Last7Days
        val version = Highlight.Version("3.3.0")

        lateinit var analyticsRepository: AnalyticsRepository
        lateinit var highlightsRepository: HighlightsRepository

        fun subject(): LoadProjectHighlights {
            analyticsRepository = mock<AnalyticsRepository>()
            highlightsRepository = mock<HighlightsRepository>()
            everySuspend { analyticsRepository.getOverview(any(), any()) } returns
                Either.Right(TestAnalyticsSnapshot.overview)
            everySuspend { highlightsRepository.getLatestAppVersion(any()) } returns Either.Right(version)
            everySuspend { highlightsRepository.getRevenue(any()) } returns Either.Left(DataError.Forbidden)
            return LoadProjectHighlights(analyticsRepository, highlightsRepository)
        }

        should("request only the sources the metrics need, in the metrics' order") {
            val loaded = subject()(projectId, range, listOf(HighlightMetric.LatestAppVersion, HighlightMetric.Visitors))
            loaded.keys.toList() shouldBe listOf(HighlightMetric.LatestAppVersion, HighlightMetric.Visitors)
            loaded.getValue(HighlightMetric.LatestAppVersion) shouldBe Either.Right(version)
            val visitors = TestAnalyticsSnapshot.overview.first { it.key == OverviewMetricKey.Visitors }
            loaded.getValue(HighlightMetric.Visitors) shouldBe Either.Right(
                Highlight.Figure(visitors.value!!, visitors.kind, visitors.changePercent, visitors.isIncreaseBad),
            )
            verifySuspend(not) { highlightsRepository.getActiveUsers(any()) }
            verifySuspend(not) { highlightsRepository.getRevenue(any()) }
            verifySuspend(not) { highlightsRepository.getExceptions(any(), any()) }
        }

        should("fail only the metrics whose source failed") {
            val loaded = subject()(projectId, range, listOf(HighlightMetric.Mrr, HighlightMetric.LatestAppVersion))
            loaded.getValue(HighlightMetric.Mrr) shouldBe Either.Left(DataError.Forbidden)
            loaded.getValue(HighlightMetric.LatestAppVersion) shouldBe Either.Right(version)
        }
    })
