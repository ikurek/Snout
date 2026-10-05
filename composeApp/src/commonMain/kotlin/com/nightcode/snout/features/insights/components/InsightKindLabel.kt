package com.nightcode.snout.features.insights.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_kind_funnels
import snout.composeapp.generated.resources.insight_kind_lifecycle
import snout.composeapp.generated.resources.insight_kind_other
import snout.composeapp.generated.resources.insight_kind_paths
import snout.composeapp.generated.resources.insight_kind_retention
import snout.composeapp.generated.resources.insight_kind_sql
import snout.composeapp.generated.resources.insight_kind_stickiness
import snout.composeapp.generated.resources.insight_kind_trends

@Composable
internal fun InsightKind.label(): String = stringResource(
    when (this) {
        InsightKind.Trends -> Res.string.insight_kind_trends
        InsightKind.Funnels -> Res.string.insight_kind_funnels
        InsightKind.Retention -> Res.string.insight_kind_retention
        InsightKind.Paths -> Res.string.insight_kind_paths
        InsightKind.Stickiness -> Res.string.insight_kind_stickiness
        InsightKind.Lifecycle -> Res.string.insight_kind_lifecycle
        InsightKind.Sql -> Res.string.insight_kind_sql
        InsightKind.Unknown -> Res.string.insight_kind_other
    },
)
