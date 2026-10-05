package com.nightcode.snout.shared.domain.model.insights

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.serialization.Serializable
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

@Serializable
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
)
