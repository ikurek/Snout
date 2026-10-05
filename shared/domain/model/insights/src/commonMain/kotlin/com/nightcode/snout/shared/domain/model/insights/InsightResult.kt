package com.nightcode.snout.shared.domain.model.insights

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.serialization.Serializable

@Serializable
sealed interface InsightResult {

    /** Trends, stickiness and lifecycle: one or more series over the same periods. */
    @Serializable
    data class Series(val series: List<TrendSeries>) : InsightResult

    @Serializable
    data class Funnel(val steps: List<FunnelStep>) : InsightResult

    /** SQL and table displays: already stringified cells. */
    @Serializable
    data class Table(val columns: List<String>, val rows: List<List<String>>) : InsightResult

    @Serializable
    data object Unsupported : InsightResult
}

@Serializable
data class TrendSeries(
    val label: String,
    val values: List<Double>,
    val periods: List<String>,
    val aggregatedValue: Double?,
) {
    val total: Double get() = aggregatedValue ?: values.sum()
}

@Serializable
data class FunnelStep(
    val name: String,
    val count: Double,
    /** Share of the first step that reached this step, 0..1. */
    val conversionRate: Double,
    val medianConversionSeconds: Double?,
)

@Serializable
data class InsightDetail(val summary: InsightSummary, val result: InsightResult?)

@TestData
val TestSeriesResult = InsightResult.Series(
    series = listOf(
        TrendSeries(
            label = "\$pageview",
            values = listOf(120.0, 180.0, 90.0, 240.0, 200.0, 310.0, 280.0),
            periods = listOf(
                "2026-09-29",
                "2026-09-30",
                "2026-10-01",
                "2026-10-02",
                "2026-10-03",
                "2026-10-04",
                "2026-10-05",
            ),
            aggregatedValue = null,
        ),
        TrendSeries(
            label = "sign up",
            values = listOf(20.0, 35.0, 10.0, 60.0, 45.0, 70.0, 66.0),
            periods = listOf(
                "2026-09-29",
                "2026-09-30",
                "2026-10-01",
                "2026-10-02",
                "2026-10-03",
                "2026-10-04",
                "2026-10-05",
            ),
            aggregatedValue = null,
        ),
    ),
)

@TestData
val TestFunnelResult = InsightResult.Funnel(
    steps = listOf(
        FunnelStep("Visited pricing", 1200.0, 1.0, null),
        FunnelStep("Started checkout", 480.0, 0.4, 95.0),
        FunnelStep("Paid", 210.0, 0.175, 300.0),
    ),
)

@TestData
@OptIn(TestData::class)
val TestInsightDetail = InsightDetail(summary = TestInsightSummary, result = TestSeriesResult)
