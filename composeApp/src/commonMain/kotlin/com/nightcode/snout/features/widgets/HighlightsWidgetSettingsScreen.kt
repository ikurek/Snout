package com.nightcode.snout.features.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.AppTheme
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MenuOption
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.analytics.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.HighlightSource
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.widgets.HighlightsWidgetConfig
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.analytics_metric_bounce_rate
import snout.composeapp.generated.resources.analytics_metric_mau
import snout.composeapp.generated.resources.analytics_metric_session_duration
import snout.composeapp.generated.resources.analytics_metric_sessions
import snout.composeapp.generated.resources.analytics_metric_views
import snout.composeapp.generated.resources.analytics_metric_visitors
import snout.composeapp.generated.resources.analytics_metric_wau
import snout.composeapp.generated.resources.dialog_cancel
import snout.composeapp.generated.resources.ic_analytics_24dp
import snout.composeapp.generated.resources.widget_analytics_settings_add
import snout.composeapp.generated.resources.widget_analytics_settings_range_section
import snout.composeapp.generated.resources.widget_analytics_settings_save
import snout.composeapp.generated.resources.widget_highlight_app_version
import snout.composeapp.generated.resources.widget_highlight_dau
import snout.composeapp.generated.resources.widget_highlight_exceptions
import snout.composeapp.generated.resources.widget_highlight_mrr
import snout.composeapp.generated.resources.widget_highlight_revenue
import snout.composeapp.generated.resources.widget_highlights_group_app
import snout.composeapp.generated.resources.widget_highlights_group_revenue
import snout.composeapp.generated.resources.widget_highlights_group_users
import snout.composeapp.generated.resources.widget_highlights_group_web
import snout.composeapp.generated.resources.widget_highlights_metrics_hint
import snout.composeapp.generated.resources.widget_highlights_revenue_hint
import snout.composeapp.generated.resources.widget_highlights_settings_title
import snout.composeapp.generated.resources.widget_insight_picker_not_connected_message
import snout.composeapp.generated.resources.widget_insight_picker_not_connected_title
import snout.composeapp.generated.resources.widget_project_title

/** The metrics' groups as the settings list them, each under its own label. */
private val MetricGroups: List<Pair<StringResource, List<HighlightMetric>>> = listOf(
    Res.string.widget_highlights_group_web to listOf(
        HighlightMetric.Visitors,
        HighlightMetric.Views,
        HighlightMetric.Sessions,
        HighlightMetric.SessionDuration,
        HighlightMetric.BounceRate,
    ),
    Res.string.widget_highlights_group_users to listOf(
        HighlightMetric.DailyActiveUsers,
        HighlightMetric.WeeklyActiveUsers,
        HighlightMetric.MonthlyActiveUsers,
    ),
    Res.string.widget_highlights_group_revenue to listOf(HighlightMetric.Mrr, HighlightMetric.Revenue),
    Res.string.widget_highlights_group_app to listOf(HighlightMetric.LatestAppVersion, HighlightMetric.Exceptions),
)

/**
 * The settings an Android project highlights widget opens when it is added or reconfigured, in the app's theme: its
 * project, its range and its metrics. [initial] is null while the widget is being added, which the button says.
 */
@Composable
fun HighlightsWidgetSettings(
    initial: HighlightsWidgetConfig?,
    onSave: (HighlightsWidgetConfig) -> Unit,
    onClose: () -> Unit,
) {
    val viewModel = koinViewModel<WidgetSettingsViewModel> { parametersOf(WidgetProject(initial?.projectId)) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var range by rememberSaveable { mutableStateOf(initial?.range ?: HighlightsWidgetConfig.DefaultRange) }
    // Names, so the picks survive the activity being recreated.
    var metricNames by rememberSaveable {
        mutableStateOf((initial?.metrics ?: HighlightsWidgetConfig.DefaultMetrics).map { it.name })
    }
    val metrics = metricNames.mapNotNull { name -> HighlightMetric.entries.firstOrNull { it.name == name } }
    AppTheme(initialThemeMode = ThemeMode.System) {
        Surface {
            HighlightsWidgetSettingsScreen(
                state = state,
                range = range,
                metrics = metrics,
                isNew = initial == null,
                onProjectSelected = viewModel::onProjectSelected,
                onRetryProjects = viewModel::retryProjects,
                onRangeSelected = { range = it },
                // Kept in the settings' order, which the widget shows them in.
                onMetricToggled = { metric, on ->
                    val picked = if (on) metrics + metric else metrics - metric
                    metricNames = HighlightMetric.entries.filter { it in picked }.map { it.name }
                },
                onSave = {
                    val project = state.projects?.firstOrNull { it.id == state.projectId }
                    project?.let { onSave(HighlightsWidgetConfig(it.id, range, metrics, projectName = it.name)) }
                },
                onClose = onClose,
            )
        }
    }
}

@Composable
internal fun HighlightsWidgetSettingsScreen(
    state: WidgetSettingsUiState,
    range: AnalyticsRange,
    metrics: List<HighlightMetric>,
    isNew: Boolean,
    onProjectSelected: (Long) -> Unit,
    onRetryProjects: () -> Unit,
    onRangeSelected: (AnalyticsRange) -> Unit,
    onMetricToggled: (HighlightMetric, Boolean) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    PushedScreenScaffold(
        title = stringResource(Res.string.widget_highlights_settings_title),
        onNavigateBack = onClose,
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom())
        when (state.isConnected) {
            null -> LoadingOrErrorContent(error = null, onRetry = {}, modifier = modifier)

            false -> Placeholder(
                icon = painterResource(Res.drawable.ic_analytics_24dp),
                title = stringResource(Res.string.widget_insight_picker_not_connected_title),
                message = stringResource(Res.string.widget_insight_picker_not_connected_message),
                actionLabel = stringResource(Res.string.dialog_cancel),
                onAction = onClose,
                modifier = modifier.wrapContentHeight(),
            )

            true -> Column(
                modifier = modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding.bottomOnly())
                    .padding(bottom = Spacing.xxLarge),
            ) {
                // In the order the iOS widget's settings ask: project, range, metrics.
                SectionLabel(stringResource(Res.string.widget_project_title))
                val projectsFailed = state.projectsError != null
                WidgetProjectRow(state.projects, projectsFailed, state.projectId, onProjectSelected, onRetryProjects)

                val rangeLabel = stringResource(Res.string.widget_analytics_settings_range_section)
                SectionLabel(rangeLabel)
                WidgetDropdownRow(
                    label = rangeLabel,
                    value = stringResource(range.label),
                    options = AnalyticsRange.entries.map { MenuOption(it, stringResource(it.label)) },
                    selected = range,
                    onSelect = onRangeSelected,
                )

                MetricSwitches(metrics, onMetricToggled)

                VerticalSpacer(Spacing.xxLarge)
                val action = Res.string.run {
                    if (isNew) widget_analytics_settings_add else widget_analytics_settings_save
                }
                PrimaryButton(
                    text = stringResource(action),
                    onClick = onSave,
                    enabled = metrics.isNotEmpty() && state.projects.orEmpty().any { it.id == state.projectId },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large),
                )
            }
        }
    }
}

/**
 * A switch per metric, under its group's label: up to [HighlightsWidgetConfig.MaxMetrics] on, at least one. The
 * widget shows them in this order, as many as fit.
 */
@Composable
private fun MetricSwitches(metrics: List<HighlightMetric>, onMetricToggled: (HighlightMetric, Boolean) -> Unit) {
    Text(
        text = stringResource(Res.string.widget_highlights_metrics_hint, HighlightsWidgetConfig.MaxMetrics),
        style = SnoutTheme.typography.bodyMedium,
        color = SnoutTheme.colors.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
    )
    val isFull = metrics.size >= HighlightsWidgetConfig.MaxMetrics
    MetricGroups.forEach { (group, groupMetrics) ->
        SectionLabel(stringResource(group))
        groupMetrics.forEachIndexed { index, metric ->
            val checked = metric in metrics
            val enabled = if (checked) metrics.size > 1 else !isFull
            EntityRow(
                title = stringResource(metric.label),
                subtitle = if (metric.source == HighlightSource.Revenue) {
                    stringResource(Res.string.widget_highlights_revenue_hint)
                } else {
                    null
                },
                titleStyle = SnoutTheme.typography.bodyLarge,
                divider = index < groupMetrics.lastIndex,
                onClick = if (enabled) ({ onMetricToggled(metric, !checked) }) else null,
                trailing = {
                    Switch(checked = checked, onCheckedChange = { onMetricToggled(metric, it) }, enabled = enabled)
                },
            )
        }
    }
}

internal val HighlightMetric.label: StringResource
    get() = when (this) {
        HighlightMetric.Visitors -> Res.string.analytics_metric_visitors
        HighlightMetric.Views -> Res.string.analytics_metric_views
        HighlightMetric.Sessions -> Res.string.analytics_metric_sessions
        HighlightMetric.SessionDuration -> Res.string.analytics_metric_session_duration
        HighlightMetric.BounceRate -> Res.string.analytics_metric_bounce_rate
        HighlightMetric.DailyActiveUsers -> Res.string.widget_highlight_dau
        HighlightMetric.WeeklyActiveUsers -> Res.string.analytics_metric_wau
        HighlightMetric.MonthlyActiveUsers -> Res.string.analytics_metric_mau
        HighlightMetric.Mrr -> Res.string.widget_highlight_mrr
        HighlightMetric.Revenue -> Res.string.widget_highlight_revenue
        HighlightMetric.LatestAppVersion -> Res.string.widget_highlight_app_version
        HighlightMetric.Exceptions -> Res.string.widget_highlight_exceptions
    }

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun HighlightsWidgetSettingsScreenPreview() = HighlightsWidgetSettingsScreen(
    state = WidgetSettingsUiState(isConnected = true, projects = TestProjectChoices, projectId = 1L),
    range = HighlightsWidgetConfig.DefaultRange,
    metrics = HighlightsWidgetConfig.DefaultMetrics,
    isNew = true,
    onProjectSelected = {},
    onRetryProjects = {},
    onRangeSelected = {},
    onMetricToggled = { _, _ -> },
    onSave = {},
    onClose = {},
)
