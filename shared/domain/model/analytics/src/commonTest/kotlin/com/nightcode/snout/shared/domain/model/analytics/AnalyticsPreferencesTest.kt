package com.nightcode.snout.shared.domain.model.analytics

import com.nightcode.snout.shared.common.testdata.TestData
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class AnalyticsPreferencesTest :
    ShouldSpec({
        val snapshot = TestAnalyticsSnapshot

        should("keep the whole snapshot with the default preferences") {
            snapshot.applying(AnalyticsPreferences.Default) shouldBe snapshot
        }

        should("drop the stat cards that are switched off") {
            val preferences = AnalyticsPreferences.Default
                .withMetric(OverviewMetricKey.BounceRate, enabled = false)
                .withMetric(OverviewMetricKey.Views, enabled = false)
            snapshot.applying(preferences).overview.map { it.key } shouldBe listOf(
                OverviewMetricKey.Visitors,
                OverviewMetricKey.Sessions,
                OverviewMetricKey.SessionDuration,
                OverviewMetricKey.WeeklyActiveUsers,
                OverviewMetricKey.MonthlyActiveUsers,
            )
        }

        should("always keep stat cards Snout does not know") {
            val other = snapshot.overview.first().copy(key = OverviewMetricKey.Other, label = "new metric")
            val preferences = AnalyticsPreferences.Default.copy(metrics = setOf(OverviewMetricKey.Visitors))
            snapshot.copy(overview = snapshot.overview + other).applying(preferences).overview.map { it.key } shouldBe
                listOf(OverviewMetricKey.Visitors, OverviewMetricKey.Other)
        }

        should("drop the trends when the chart is hidden") {
            val applied = snapshot.applying(AnalyticsPreferences.Default.copy(showTrendChart = false))
            applied.trends.shouldBeEmpty()
            applied.trend(TrendMetric.Visitors) shouldBe null
        }

        should("keep only the enabled breakdown tables") {
            val preferences = AnalyticsPreferences.Default.copy(breakdowns = setOf(WebBreakdown.Channel))
            snapshot.applying(preferences).breakdowns.map { it.breakdown } shouldBe listOf(WebBreakdown.Channel)
        }

        should("keep the range") {
            val preferences = AnalyticsPreferences.Default.copy(defaultRange = AnalyticsRange.Today)
            snapshot.applying(preferences).range shouldBe snapshot.range
        }

        should("toggle a metric and a breakdown") {
            val off = AnalyticsPreferences.Default
                .withMetric(OverviewMetricKey.Views, enabled = false)
                .withBreakdown(WebBreakdown.Page, enabled = false)
            (OverviewMetricKey.Views in off.metrics) shouldBe false
            (WebBreakdown.Page in off.breakdowns) shouldBe false
            off.withMetric(OverviewMetricKey.Views, enabled = true)
                .withBreakdown(WebBreakdown.Page, enabled = true) shouldBe AnalyticsPreferences.Default
        }

        should("not let other metrics be configured") {
            (OverviewMetricKey.Other in AnalyticsPreferences.ConfigurableMetrics) shouldBe false
        }
    })
