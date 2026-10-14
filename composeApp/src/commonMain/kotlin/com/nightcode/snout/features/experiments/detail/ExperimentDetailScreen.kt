package com.nightcode.snout.features.experiments.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.InfoBanner
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.organisms.DetailLoadingRowCount
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.organisms.RefreshableContent
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.experiments.components.ExperimentStatusPill
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.model.experiments.ExperimentVariant
import com.nightcode.snout.shared.domain.model.experiments.TestExperiment
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.experiment_conclusion_section
import snout.composeapp.generated.resources.experiment_created_by
import snout.composeapp.generated.resources.experiment_ended
import snout.composeapp.generated.resources.experiment_feature_flag
import snout.composeapp.generated.resources.experiment_metrics_primary
import snout.composeapp.generated.resources.experiment_metrics_secondary
import snout.composeapp.generated.resources.experiment_no_metrics
import snout.composeapp.generated.resources.experiment_overview_section
import snout.composeapp.generated.resources.experiment_results_note
import snout.composeapp.generated.resources.experiment_started_label
import snout.composeapp.generated.resources.experiment_status_label
import snout.composeapp.generated.resources.experiment_title_placeholder
import snout.composeapp.generated.resources.experiment_variants_section
import snout.composeapp.generated.resources.feature_flag_rollout_percent

@Composable
internal fun ExperimentDetailScreen(
    state: ExperimentDetailUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenFeatureFlag: (flagId: Long) -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    val experiment = state.experiment
    PushedScreenScaffold(
        title = experiment?.name ?: stringResource(Res.string.experiment_title_placeholder),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding.withoutBottom())
        if (experiment == null) {
            LoadingOrErrorContent(
                error = state.error,
                onRetry = onRefresh,
                modifier = modifier,
                loadingContent = { SkeletonRows(count = DetailLoadingRowCount) },
            )
        } else {
            RefreshableContent(
                isRefreshing = state.isRefreshing,
                refreshError = state.error,
                onRefresh = onRefresh,
                modifier = modifier,
                isPullToRefreshEnabled = false,
            ) {
                ExperimentContent(experiment, onOpenFeatureFlag, Modifier.fillMaxSize(), innerPadding.bottomOnly())
            }
        }
    }
}

@Composable
private fun ExperimentContent(
    experiment: Experiment,
    onOpenFeatureFlag: (flagId: Long) -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = Spacing.xxLarge),
    ) {
        experiment.description?.let {
            Text(
                text = it,
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
            )
        }
        OverviewSection(experiment, onOpenFeatureFlag)
        if (experiment.variants.isNotEmpty()) {
            VariantsSection(experiment.variants)
        }
        MetricsSection(experiment)
        experiment.conclusion?.let { conclusion ->
            SectionLabel(stringResource(Res.string.experiment_conclusion_section))
            KeyValueRow(
                label = conclusion.replaceFirstChar { it.uppercase() },
                value = experiment.conclusionComment.orEmpty(),
            )
        }
        InfoBanner(
            text = stringResource(Res.string.experiment_results_note),
            modifier = Modifier.padding(start = Spacing.large, end = Spacing.large, top = Spacing.xLarge),
        )
    }
}

@Composable
private fun OverviewSection(experiment: Experiment, onOpenFeatureFlag: (flagId: Long) -> Unit) {
    SectionLabel(stringResource(Res.string.experiment_overview_section))
    KeyValueRow(
        label = stringResource(Res.string.experiment_status_label),
        value = "",
        trailing = { ExperimentStatusPill(status = experiment.status) },
    )
    experiment.startDate?.let {
        KeyValueRow(label = stringResource(Res.string.experiment_started_label), value = it.formatShortDate())
    }
    experiment.endDate?.let {
        KeyValueRow(label = stringResource(Res.string.experiment_ended), value = it.formatShortDate())
    }
    experiment.featureFlagKey?.let { key ->
        val flagId = experiment.featureFlagId
        KeyValueRow(
            label = stringResource(Res.string.experiment_feature_flag),
            value = key,
            valueIsCode = true,
            onClick = flagId?.let { id -> { onOpenFeatureFlag(id) } },
        )
    }
    experiment.createdBy?.let { KeyValueRow(label = stringResource(Res.string.experiment_created_by), value = it) }
}

@Composable
private fun VariantsSection(variants: List<ExperimentVariant>) {
    SectionLabel(stringResource(Res.string.experiment_variants_section))
    variants.forEach { variant ->
        KeyValueRow(
            label = variant.name ?: variant.key,
            value = variant.key,
            valueIsCode = true,
            trailing = {
                Text(
                    text = stringResource(Res.string.feature_flag_rollout_percent, variant.rolloutPercentage),
                    style = SnoutTheme.typography.titleSmall,
                )
            },
        )
    }
}

@Composable
private fun MetricsSection(experiment: Experiment) {
    SectionLabel(stringResource(Res.string.experiment_metrics_primary))
    if (experiment.metricNames.isEmpty()) {
        Text(
            text = stringResource(Res.string.experiment_no_metrics),
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
        )
    }
    experiment.metricNames.forEach { MetricRow(it) }
    if (experiment.secondaryMetricNames.isNotEmpty()) {
        SectionLabel(stringResource(Res.string.experiment_metrics_secondary))
        experiment.secondaryMetricNames.forEach { MetricRow(it) }
    }
}

@Composable
private fun MetricRow(name: String) = Text(
    text = name,
    style = SnoutTheme.typography.bodyLarge,
    modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExperimentDetailScreenPreview() = ExperimentDetailScreen(
    state = ExperimentDetailUiState(experiment = TestExperiment),
    onNavigateBack = {},
    onRefresh = {},
    onOpenFeatureFlag = {},
    onOpenInPostHog = {},
)
