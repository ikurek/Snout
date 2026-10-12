package com.nightcode.snout.features.settings.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.SecondaryButton
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.SegmentedControl
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.analytics.label
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.settings_analytics_breakdowns_section
import snout.composeapp.generated.resources.settings_analytics_chart_label
import snout.composeapp.generated.resources.settings_analytics_chart_section
import snout.composeapp.generated.resources.settings_analytics_metrics_section
import snout.composeapp.generated.resources.settings_analytics_range_section
import snout.composeapp.generated.resources.settings_analytics_reset
import snout.composeapp.generated.resources.settings_analytics_title

private const val LoadingRowCount = 5

@Composable
internal fun AnalyticsSettingsScreen(
    preferences: AnalyticsPreferences?,
    onNavigateBack: () -> Unit,
    onDefaultRangeSelected: (AnalyticsRange) -> Unit,
    onMetricToggled: (OverviewMetricKey, Boolean) -> Unit,
    onTrendChartToggled: (Boolean) -> Unit,
    onBreakdownToggled: (WebBreakdown, Boolean) -> Unit,
    onReset: () -> Unit,
) {
    PushedScreenScaffold(
        title = stringResource(Res.string.settings_analytics_title),
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom())
        if (preferences == null) {
            SkeletonRows(modifier = modifier, count = LoadingRowCount)
        } else {
            AnalyticsSettingsContent(
                preferences = preferences,
                modifier = modifier,
                contentPadding = innerPadding.bottomOnly(),
                onDefaultRangeSelected = onDefaultRangeSelected,
                onMetricToggled = onMetricToggled,
                onTrendChartToggled = onTrendChartToggled,
                onBreakdownToggled = onBreakdownToggled,
                onReset = onReset,
            )
        }
    }
}

@Composable
private fun AnalyticsSettingsContent(
    preferences: AnalyticsPreferences,
    modifier: Modifier,
    contentPadding: PaddingValues,
    onDefaultRangeSelected: (AnalyticsRange) -> Unit,
    onMetricToggled: (OverviewMetricKey, Boolean) -> Unit,
    onTrendChartToggled: (Boolean) -> Unit,
    onBreakdownToggled: (WebBreakdown, Boolean) -> Unit,
    onReset: () -> Unit,
) {
    val rangeLabels = AnalyticsRange.entries.associateWith { it.label() }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = Spacing.xxLarge),
    ) {
        SectionLabel(stringResource(Res.string.settings_analytics_range_section))
        SegmentedControl(
            options = AnalyticsRange.entries,
            selected = preferences.defaultRange,
            onSelect = onDefaultRangeSelected,
            label = { rangeLabels.getValue(it) },
            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.xxSmall),
        )

        SectionLabel(stringResource(Res.string.settings_analytics_metrics_section))
        val metrics = AnalyticsPreferences.ConfigurableMetrics
        metrics.forEachIndexed { index, key ->
            val enabled = key in preferences.metrics
            SwitchRow(
                title = key.label(),
                checked = enabled,
                // Keep at least one stat card: the last one on cannot be switched off.
                enabled = enabled.not() || preferences.metrics.size > 1,
                divider = index < metrics.lastIndex,
                onCheckedChange = { onMetricToggled(key, it) },
            )
        }

        SectionLabel(stringResource(Res.string.settings_analytics_chart_section))
        SwitchRow(
            title = stringResource(Res.string.settings_analytics_chart_label),
            checked = preferences.showTrendChart,
            divider = false,
            onCheckedChange = onTrendChartToggled,
        )

        SectionLabel(stringResource(Res.string.settings_analytics_breakdowns_section))
        val breakdowns = WebBreakdown.entries
        breakdowns.forEachIndexed { index, breakdown ->
            SwitchRow(
                title = breakdown.label(),
                checked = breakdown in preferences.breakdowns,
                divider = index < breakdowns.lastIndex,
                onCheckedChange = { onBreakdownToggled(breakdown, it) },
            )
        }

        VerticalSpacer(Spacing.xxLarge)
        SecondaryButton(
            text = stringResource(Res.string.settings_analytics_reset),
            onClick = onReset,
            enabled = preferences != AnalyticsPreferences.Default,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large),
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    divider: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    EntityRow(
        title = title,
        titleStyle = SnoutTheme.typography.bodyLarge,
        divider = divider,
        onClick = if (enabled) ({ onCheckedChange(checked.not()) }) else null,
        trailing = { Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled) },
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnalyticsSettingsScreenPreview() = AnalyticsSettingsScreen(
    preferences = AnalyticsPreferences.Default
        .withMetric(OverviewMetricKey.BounceRate, enabled = false)
        .withBreakdown(WebBreakdown.Screen, enabled = false),
    onNavigateBack = {},
    onDefaultRangeSelected = {},
    onMetricToggled = { _, _ -> },
    onTrendChartToggled = {},
    onBreakdownToggled = { _, _ -> },
    onReset = {},
)
