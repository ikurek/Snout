package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.api.insights.model.DashboardDetailDto
import com.nightcode.snout.shared.api.insights.model.DashboardDto
import com.nightcode.snout.shared.api.insights.model.DashboardTileDto
import com.nightcode.snout.shared.api.insights.model.InsightDto
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.InsightSummary

internal fun InsightDto.toSummary(): InsightSummary {
    val kind = query.insightKind()
    return InsightSummary(
        id = id,
        shortId = shortId,
        name = name?.takeIf { it.isNotBlank() } ?: derivedName?.takeIf { it.isNotBlank() } ?: "Untitled insight",
        description = description?.takeIf { it.isNotBlank() },
        favorited = favorited,
        lastModifiedAt = lastModifiedAt,
        tags = tags,
        kind = kind,
        display = query.insightDisplay(kind),
    )
}

internal fun InsightDto.toResult(summary: InsightSummary = toSummary()): InsightResult? =
    result.toInsightResult(summary.kind, summary.display, columns)

internal fun InsightDto.toDetail(): InsightDetail {
    val summary = toSummary()
    return InsightDetail(summary = summary, result = toResult(summary))
}

internal fun DashboardDto.toSummary(): DashboardSummary = DashboardSummary(
    id = id,
    name = name,
    description = description?.takeIf { it.isNotBlank() },
    pinned = pinned,
    lastAccessedAt = lastAccessedAt,
    tags = tags,
)

internal fun DashboardDetailDto.toDashboard(): Dashboard = Dashboard(
    id = id,
    name = name,
    description = description?.takeIf { it.isNotBlank() },
    tiles = tiles.mapNotNull { it.toTile() },
)

private fun DashboardTileDto.toTile(): DashboardTile? {
    val insightDto = insight
    val textDto = text
    return when {
        insightDto != null -> {
            val summary = insightDto.toSummary()
            DashboardTile.InsightTile(id = id, insight = summary, result = insightDto.toResult(summary))
        }

        textDto != null -> DashboardTile.TextTile(id = id, body = textDto.body)

        else -> null
    }
}
