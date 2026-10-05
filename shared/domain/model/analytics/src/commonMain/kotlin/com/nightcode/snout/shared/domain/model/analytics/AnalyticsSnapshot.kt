package com.nightcode.snout.shared.domain.model.analytics

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsSnapshot(
    val range: AnalyticsRange,
    val overview: List<OverviewMetric>,
    val visitorsTrend: TimeSeries?,
    val breakdowns: List<BreakdownTable>,
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
    ),
    visitorsTrend = TimeSeries(
        points = listOf(
            TimeSeriesPoint("2026-09-29", 1_500.0),
            TimeSeriesPoint("2026-09-30", 1_720.0),
            TimeSeriesPoint("2026-10-01", 1_650.0),
            TimeSeriesPoint("2026-10-02", 1_980.0),
            TimeSeriesPoint("2026-10-03", 1_210.0),
            TimeSeriesPoint("2026-10-04", 1_890.0),
            TimeSeriesPoint("2026-10-05", 2_480.0),
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
            breakdown = WebBreakdown.ReferringDomain,
            rows = listOf(BreakdownRow("google.com", 6_100.0, null), BreakdownRow("\$direct", 4_300.0, null)),
        ),
    ),
)
