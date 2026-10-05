package com.nightcode.snout.shared.domain.model.insights

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DashboardSummary(
    val id: Long,
    val name: String,
    val description: String?,
    val pinned: Boolean,
    val lastAccessedAt: Instant?,
    val tags: List<String>,
)

@Serializable
data class Dashboard(val id: Long, val name: String, val description: String?, val tiles: List<DashboardTile>)

@Serializable
sealed interface DashboardTile {
    val id: Long

    @Serializable
    data class InsightTile(override val id: Long, val insight: InsightSummary, val result: InsightResult?) :
        DashboardTile

    @Serializable
    data class TextTile(override val id: Long, val body: String) : DashboardTile
}

@TestData
val TestDashboardSummary = DashboardSummary(
    id = 1L,
    name = "Product overview",
    description = "Key metrics for the mobile app",
    pinned = true,
    lastAccessedAt = Instant.parse("2026-10-04T08:00:00Z"),
    tags = listOf("mobile", "weekly"),
)

@TestData
@OptIn(TestData::class)
val TestDashboard = Dashboard(
    id = 1L,
    name = "Product overview",
    description = "Key metrics for the mobile app",
    tiles = listOf(
        DashboardTile.TextTile(id = 1L, body = "Numbers refresh every morning."),
        DashboardTile.InsightTile(id = 2L, insight = TestInsightSummary, result = TestSeriesResult),
        DashboardTile.InsightTile(
            id = 3L,
            insight = TestInsightSummary.copy(
                id = 11L,
                name = "Checkout funnel",
                kind = InsightKind.Funnels,
                display = InsightDisplay.Funnel,
            ),
            result = TestFunnelResult,
        ),
    ),
)
