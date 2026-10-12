package com.nightcode.snout.shared.domain.model.insights

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.time.Instant

enum class InsightKind {
    Trends,
    Funnels,
    Retention,
    Paths,
    Stickiness,
    Lifecycle,
    Sql,
    Unknown,
}

/** How PostHog wants the result drawn; Snout maps unsupported displays to the closest readable form. */
enum class InsightDisplay {
    Line,
    Bar,
    BarValue,
    Number,
    Pie,
    Table,
    Funnel,
    Unsupported,
}

enum class PeriodUnit {
    Hour,
    Day,
    Week,
    Month,
    Year,
}

/** The dates an insight covers, as PostHog's date filter describes them. */
sealed interface InsightDateRange {
    data class Last(val amount: Int, val unit: PeriodUnit) : InsightDateRange

    data object Today : InsightDateRange

    data object Yesterday : InsightDateRange

    data object MonthToDate : InsightDateRange

    data object YearToDate : InsightDateRange

    data object AllTime : InsightDateRange

    /** ISO dates; [to] is null for "until now". */
    data class Between(val from: String, val to: String?) : InsightDateRange
}

/** What the numbers cover: the date range, how points are bucketed and whether a previous period is drawn. */
data class InsightPeriod(val range: InsightDateRange, val interval: PeriodUnit? = null, val compared: Boolean = false)

data class InsightSummary(
    val id: Long,
    val shortId: String,
    val name: String,
    val description: String?,
    val favorited: Boolean,
    val lastModifiedAt: Instant?,
    val tags: List<String>,
    val kind: InsightKind,
    val display: InsightDisplay,
    val period: InsightPeriod? = null,
)

@TestData
val TestInsightSummary = InsightSummary(
    id = 10L,
    shortId = "abc123",
    name = "Daily active users",
    description = "Unique users firing any event per day",
    favorited = true,
    lastModifiedAt = Instant.parse("2026-10-01T10:00:00Z"),
    tags = listOf("growth"),
    kind = InsightKind.Trends,
    display = InsightDisplay.Line,
    period = InsightPeriod(InsightDateRange.Last(7, PeriodUnit.Day), PeriodUnit.Day),
)
