package com.nightcode.snout.features.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.nightcode.snout.designsystem.atoms.TrendChip
import com.nightcode.snout.designsystem.charts.BoldNumber
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_result_versus

@Composable
internal fun NumberResultView(series: List<TrendSeries>, formatter: (Double) -> String, expandable: Boolean) {
    val single = series.singleOrNull()
    if (single != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            BoldNumber(value = formatter(single.total), label = single.label.ifEmpty { null })
            single.previous?.let { previous ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall),
                ) {
                    single.changePercent?.let { TrendChip(changePercent = it) }
                    Text(
                        text = stringResource(Res.string.insight_result_versus, formatter(previous.total)),
                        style = SnoutTheme.typography.bodySmall,
                        color = SnoutTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
        return
    }
    ExpandableRows(items = series, expandable = expandable) { rows ->
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            rows.forEach { s ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    Text(
                        text = s.label,
                        style = SnoutTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    s.changePercent?.let { TrendChip(changePercent = it) }
                    Text(text = formatter(s.total), style = SnoutTheme.typography.headlineMedium)
                }
            }
        }
    }
}
