package com.nightcode.snout.shared.domain.model.analytics

import com.nightcode.snout.shared.common.testdata.TestData

data class AnalyticsSnapshot(
    val range: AnalyticsRange,
    val overview: List<OverviewMetric>,
    val trends: List<MetricTrend>,
    val breakdowns: List<BreakdownTable>,
) {
    fun trend(metric: TrendMetric): MetricTrend? = trends.firstOrNull { it.metric == metric }
}

private val TestDays = listOf(
    "2026-09-29",
    "2026-09-30",
    "2026-10-01",
    "2026-10-02",
    "2026-10-03",
    "2026-10-04",
    "2026-10-05",
)

private fun testSeries(values: List<Double>) = TimeSeries(
    TestDays.zip(values) { day, value -> TimeSeriesPoint(day, value) },
)

@TestData
val TestAnalyticsSnapshot = AnalyticsSnapshot(
    range = AnalyticsRange.Last7Days,
    overview = listOf(
        OverviewMetric(
            OverviewMetricKey.Visitors,
            "visitors",
            12_430.0,
            11_020.0,
            12.8,
            OverviewMetricKind.Count,
            false,
        ),
        OverviewMetric(OverviewMetricKey.Views, "views", 48_211.0, 51_002.0, -5.5, OverviewMetricKind.Count, false),
        OverviewMetric(
            OverviewMetricKey.Sessions,
            "sessions",
            15_002.0,
            14_980.0,
            0.1,
            OverviewMetricKind.Count,
            false,
        ),
        OverviewMetric(
            OverviewMetricKey.SessionDuration,
            "session duration",
            143.0,
            120.0,
            19.2,
            OverviewMetricKind.DurationSeconds,
            false,
        ),
        OverviewMetric(
            OverviewMetricKey.BounceRate,
            "bounce rate",
            0.42,
            0.39,
            7.7,
            OverviewMetricKind.Percentage,
            true,
        ),
        OverviewMetric(
            OverviewMetricKey.WeeklyActiveUsers,
            "weekly active users",
            9_870.0,
            9_120.0,
            8.2,
            OverviewMetricKind.Count,
            false,
        ),
        OverviewMetric(
            OverviewMetricKey.MonthlyActiveUsers,
            "monthly active users",
            31_400.0,
            32_050.0,
            -2.0,
            OverviewMetricKind.Count,
            false,
        ),
    ),
    trends = listOf(
        MetricTrend(
            metric = TrendMetric.Visitors,
            current = testSeries(listOf(1_500.0, 1_720.0, 1_650.0, 1_980.0, 1_210.0, 1_890.0, 2_480.0)),
            previous = testSeries(listOf(1_400.0, 1_510.0, 1_620.0, 1_480.0, 1_300.0, 1_610.0, 1_700.0)),
        ),
        MetricTrend(
            metric = TrendMetric.Views,
            current = testSeries(listOf(6_100.0, 6_900.0, 6_400.0, 7_700.0, 4_900.0, 7_300.0, 8_900.0)),
            previous = testSeries(listOf(6_900.0, 7_200.0, 7_600.0, 7_100.0, 6_800.0, 7_400.0, 8_000.0)),
        ),
        MetricTrend(
            metric = TrendMetric.Sessions,
            current = testSeries(listOf(1_900.0, 2_100.0, 2_000.0, 2_400.0, 1_500.0, 2_300.0, 2_800.0)),
        ),
    ),
    breakdowns = listOf(
        BreakdownTable(
            breakdown = WebBreakdown.Page,
            rows = listOf(
                BreakdownRow("/", 8_200.0, 20_100.0),
                BreakdownRow("/pricing", 3_100.0, 5_400.0),
                BreakdownRow("/docs/getting-started", 1_900.0, 4_200.0),
            ),
        ),
        BreakdownTable(
            breakdown = WebBreakdown.Channel,
            rows = listOf(
                BreakdownRow("Organic Search", 5_400.0, null),
                BreakdownRow("Direct", 4_300.0, null),
                BreakdownRow("Referral", 1_200.0, null),
            ),
        ),
        BreakdownTable(
            breakdown = WebBreakdown.ReferringDomain,
            rows = listOf(BreakdownRow("google.com", 6_100.0, null), BreakdownRow("\$direct", 4_300.0, null)),
        ),
    ),
)
