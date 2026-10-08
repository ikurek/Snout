package com.nightcode.snout.shared.domain.model.insights

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.serialization.Serializable

@Serializable
sealed interface InsightResult {

    /** Trends, stickiness and lifecycle: one or more series over the same periods. */
    @Serializable
    data class Series(val series: List<TrendSeries>, val format: ValueFormat = ValueFormat()) : InsightResult

    @Serializable
    data class Funnel(val steps: List<FunnelStep>) : InsightResult

    /** One cohort per start period; [intervals] label the columns ("Day 0", "Day 1", ...). */
    @Serializable
    data class Retention(val intervals: List<String>, val cohorts: List<RetentionCohort>) : InsightResult {

        /** Share of users still active per interval, weighted by cohort size over the cohorts that reached it. */
        val curve: List<Double>
            get() = intervals.indices.map { interval ->
                val reached = cohorts.filter { it.size > 0 && interval < it.counts.size }
                val total = reached.sumOf { it.size }
                if (total > 0) reached.sumOf { it.counts[interval] } / total else 0.0
            }
    }

    /** SQL and table displays: already stringified cells; [totalRows] counts rows dropped from [rows]. */
    @Serializable
    data class Table(val columns: List<String>, val rows: List<List<String>>, val totalRows: Int = rows.size) :
        InsightResult

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
data class RetentionCohort(val date: String, val counts: List<Double>) {
    val size: Double get() = counts.firstOrNull() ?: 0.0

    /** Share of the cohort active in [interval], 0..1. */
    fun rate(interval: Int): Double? = counts.getOrNull(interval)?.let { if (size > 0) it / size else 0.0 }
}

/** PostHog's y-axis format: how a value is written, plus the user's prefix, postfix and decimal places. */
@Serializable
data class ValueFormat(
    val unit: ValueUnit = ValueUnit.Numeric,
    val prefix: String? = null,
    val postfix: String? = null,
    val decimalPlaces: Int? = null,
)

enum class ValueUnit {
    Numeric,

    /** Seconds. */
    Duration,
    DurationMs,

    /** Already in percent, 46 -> 46%. */
    Percentage,

    /** A fraction, 0.46 -> 46%. */
    PercentageScaled,
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
val TestRetentionResult = InsightResult.Retention(
    intervals = listOf("Day 0", "Day 1", "Day 2", "Day 3"),
    cohorts = listOf(
        RetentionCohort("2026-10-01T00:00:00Z", listOf(400.0, 180.0, 120.0, 96.0)),
        RetentionCohort("2026-10-02T00:00:00Z", listOf(360.0, 150.0, 100.0)),
        RetentionCohort("2026-10-03T00:00:00Z", listOf(420.0, 200.0)),
        RetentionCohort("2026-10-04T00:00:00Z", listOf(380.0)),
    ),
)

@TestData
@OptIn(TestData::class)
val TestInsightDetail = InsightDetail(summary = TestInsightSummary, result = TestSeriesResult)
