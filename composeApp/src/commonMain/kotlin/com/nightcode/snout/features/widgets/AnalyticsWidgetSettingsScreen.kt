package com.nightcode.snout.features.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
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
import com.nightcode.snout.designsystem.molecules.MenuOption
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.analytics.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.widgets.AnalyticsWidgetConfig
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.dialog_cancel
import snout.composeapp.generated.resources.ic_analytics_24dp
import snout.composeapp.generated.resources.widget_analytics_settings_add
import snout.composeapp.generated.resources.widget_analytics_settings_range_section
import snout.composeapp.generated.resources.widget_analytics_settings_save
import snout.composeapp.generated.resources.widget_analytics_settings_stat_section
import snout.composeapp.generated.resources.widget_analytics_settings_title
import snout.composeapp.generated.resources.widget_insight_picker_not_connected_message
import snout.composeapp.generated.resources.widget_insight_picker_not_connected_title
import snout.composeapp.generated.resources.widget_project_title

/**
 * The settings an Android analytics widget opens when it is added or reconfigured, in the app's theme: its project,
 * the stat it leads with and its range. [initial] is null while the widget is being added, which the button says.
 */
@Composable
fun AnalyticsWidgetSettings(
    initial: AnalyticsWidgetConfig?,
    onSave: (AnalyticsWidgetConfig) -> Unit,
    onClose: () -> Unit,
) {
    val viewModel = koinViewModel<WidgetSettingsViewModel> { parametersOf(WidgetProject(initial?.projectId)) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var metric by rememberSaveable { mutableStateOf(initial?.metric ?: AnalyticsWidgetConfig.DefaultMetric) }
    var range by rememberSaveable { mutableStateOf(initial?.range ?: AnalyticsWidgetConfig.DefaultRange) }
    AppTheme(initialThemeMode = ThemeMode.System) {
        Surface {
            AnalyticsWidgetSettingsScreen(
                state = state,
                metric = metric,
                range = range,
                isNew = initial == null,
                onProjectSelected = viewModel::onProjectSelected,
                onRetryProjects = viewModel::retryProjects,
                onMetricSelected = { metric = it },
                onRangeSelected = { range = it },
                onSave = {
                    val project = state.projects?.firstOrNull { it.id == state.projectId }
                    project?.let { onSave(AnalyticsWidgetConfig(it.id, metric, range, projectName = it.name)) }
                },
                onClose = onClose,
            )
        }
    }
}

@Composable
internal fun AnalyticsWidgetSettingsScreen(
    state: WidgetSettingsUiState,
    metric: OverviewMetricKey,
    range: AnalyticsRange,
    isNew: Boolean,
    onProjectSelected: (Long) -> Unit,
    onRetryProjects: () -> Unit,
    onMetricSelected: (OverviewMetricKey) -> Unit,
    onRangeSelected: (AnalyticsRange) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    PushedScreenScaffold(
        title = stringResource(Res.string.widget_analytics_settings_title),
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
                // Dropdowns, in the order the iOS widget's settings ask: project, stat, range.
                SectionLabel(stringResource(Res.string.widget_project_title))
                val projectsFailed = state.projectsError != null
                WidgetProjectRow(state.projects, projectsFailed, state.projectId, onProjectSelected, onRetryProjects)

                val statLabel = stringResource(Res.string.widget_analytics_settings_stat_section)
                SectionLabel(statLabel)
                WidgetDropdownRow(
                    label = statLabel,
                    value = stringResource(metric.label),
                    options = AnalyticsWidgetConfig.Metrics.map { MenuOption(it, stringResource(it.label)) },
                    selected = metric,
                    onSelect = onMetricSelected,
                )

                val rangeLabel = stringResource(Res.string.widget_analytics_settings_range_section)
                SectionLabel(rangeLabel)
                WidgetDropdownRow(
                    label = rangeLabel,
                    value = stringResource(range.label),
                    options = AnalyticsRange.entries.map { MenuOption(it, stringResource(it.label)) },
                    selected = range,
                    onSelect = onRangeSelected,
                )

                VerticalSpacer(Spacing.xxLarge)
                val action = Res.string.run {
                    if (isNew) widget_analytics_settings_add else widget_analytics_settings_save
                }
                PrimaryButton(
                    text = stringResource(action),
                    onClick = onSave,
                    enabled = state.projects.orEmpty().any { it.id == state.projectId },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large),
                )
            }
        }
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnalyticsWidgetSettingsScreenPreview() = AnalyticsWidgetSettingsScreen(
    state = WidgetSettingsUiState(isConnected = true, projects = TestProjectChoices, projectId = 1L),
    metric = AnalyticsWidgetConfig.DefaultMetric,
    range = AnalyticsWidgetConfig.DefaultRange,
    isNew = true,
    onProjectSelected = {},
    onRetryProjects = {},
    onMetricSelected = {},
    onRangeSelected = {},
    onSave = {},
    onClose = {},
)
