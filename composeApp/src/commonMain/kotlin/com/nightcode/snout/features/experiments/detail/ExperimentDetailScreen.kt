package com.nightcode.snout.features.experiments.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.SectionTitle
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.InfoNote
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.organisms.ErrorPlaceholder
import com.nightcode.snout.designsystem.organisms.ListScreenScaffold
import com.nightcode.snout.designsystem.LoadingIndicator
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.experiments.components.ExperimentStatusChip
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.model.experiments.ExperimentVariant
import com.nightcode.snout.shared.domain.model.experiments.TestExperiment
import org.jetbrains.compose.resources.painterResource
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
import snout.composeapp.generated.resources.ic_chevron_right_24dp

@Composable
internal fun ExperimentDetailScreen(
    state: CachedUiState<Experiment>,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenFeatureFlag: (flagId: Long) -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    val experiment = (state as? CachedUiState.Content)?.data
    ListScreenScaffold(
        title = experiment?.name ?: stringResource(Res.string.experiment_title_placeholder),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding)
        when (state) {
            CachedUiState.Loading, is CachedUiState.Empty -> LoadingIndicator(modifier)
            is CachedUiState.Error -> ErrorPlaceholder(error = state.error, onRetry = onRetry, modifier = modifier)
            is CachedUiState.Content -> ExperimentContent(state.data, onOpenFeatureFlag, modifier)
        }
    }
}

@Composable
private fun ExperimentContent(experiment: Experiment, onOpenFeatureFlag: (flagId: Long) -> Unit, modifier: Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        experiment.description?.let {
            Text(
                text = it,
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        OverviewSection(experiment, onOpenFeatureFlag)
        if (experiment.variants.isNotEmpty()) {
            VariantsSection(experiment.variants)
        }
        MetricsSection(experiment)
        experiment.conclusion?.let { conclusion ->
            SectionTitle(stringResource(Res.string.experiment_conclusion_section))
            KeyValueRow(
                label = conclusion.replaceFirstChar { it.uppercase() },
                value = experiment.conclusionComment.orEmpty(),
            )
        }
    }
}

@Composable
private fun OverviewSection(experiment: Experiment, onOpenFeatureFlag: (flagId: Long) -> Unit) {
    SectionTitle(stringResource(Res.string.experiment_overview_section))
    KeyValueRow(
        label = stringResource(Res.string.experiment_status_label),
        value = "",
        trailing = { ExperimentStatusChip(status = experiment.status) },
    )
    experiment.startDate?.let {
        KeyValueRow(label = stringResource(Res.string.experiment_started_label), value = it.formatShortDate())
    }
    experiment.endDate?.let {
        KeyValueRow(
            label = stringResource(Res.string.experiment_ended),
            value = it.formatShortDate(),
        )
    }
    experiment.featureFlagKey?.let { key ->
        val flagId = experiment.featureFlagId
        KeyValueRow(
            label = stringResource(Res.string.experiment_feature_flag),
            value = key,
            onClick = flagId?.let { id -> { onOpenFeatureFlag(id) } },
            trailing = flagId?.let {
                {
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right_24dp),
                        contentDescription = null,
                        tint = SnoutTheme.colors.onSurfaceVariant,
                    )
                }
            },
        )
    }
    experiment.createdBy?.let { KeyValueRow(label = stringResource(Res.string.experiment_created_by), value = it) }
}

@Composable
private fun VariantsSection(variants: List<ExperimentVariant>) {
    SectionTitle(stringResource(Res.string.experiment_variants_section))
    variants.forEach { variant ->
        KeyValueRow(
            label = variant.name ?: variant.key,
            value = variant.key,
            trailing = {
                Text(
                    text = stringResource(Res.string.feature_flag_rollout_percent, variant.rolloutPercentage),
                    style = SnoutTheme.code.medium,
                )
            },
        )
    }
}

@Composable
private fun MetricsSection(experiment: Experiment) {
    SectionTitle(stringResource(Res.string.experiment_metrics_primary))
    if (experiment.metricNames.isEmpty()) {
        Text(
            text = stringResource(Res.string.experiment_no_metrics),
            style = SnoutTheme.typography.bodySmall,
            color = SnoutTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
    experiment.metricNames.forEach { MetricRow(it) }
    if (experiment.secondaryMetricNames.isNotEmpty()) {
        SectionTitle(stringResource(Res.string.experiment_metrics_secondary))
        experiment.secondaryMetricNames.forEach { MetricRow(it) }
    }
    InfoNote(
        text = stringResource(Res.string.experiment_results_note),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun MetricRow(name: String) = EntityRow(title = name, titleStyle = SnoutTheme.typography.bodyLarge)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExperimentDetailScreenPreview() = ExperimentDetailScreen(
    state = CachedUiState.Content(TestExperiment, isRefreshing = false),
    onNavigateBack = {},
    onRetry = {},
    onOpenFeatureFlag = {},
    onOpenInPostHog = {},
)
