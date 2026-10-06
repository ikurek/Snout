package com.nightcode.snout.features.experiments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.organisms.ListScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.experiments.components.ExperimentStatusChip
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.model.experiments.ExperimentStatus
import com.nightcode.snout.shared.domain.model.experiments.TestExperiment
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.experiment_started
import snout.composeapp.generated.resources.more_experiments_title

@Composable
internal fun ExperimentsScreen(
    state: CachedUiState<List<Experiment>>,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenExperiment: (experimentId: Long) -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    ListScreenScaffold(
        title = stringResource(Res.string.more_experiments_title),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        CachedStateContainer(
            state = state,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(it, key = { experiment -> experiment.id }) { experiment ->
                    ExperimentRow(experiment = experiment, onClick = { onOpenExperiment(experiment.id) })
                }
            }
        }
    }
}

@Composable
private fun ExperimentRow(experiment: Experiment, onClick: () -> Unit) {
    EntityRow(
        title = experiment.name,
        subtitle = experiment.featureFlagKey,
        subtitleStyle = SnoutTheme.code.small,
        onClick = onClick,
        meta = experiment.startDate?.let {
            { MetaText(stringResource(Res.string.experiment_started, it.formatShortDate())) }
        },
        trailing = { ExperimentStatusChip(status = experiment.status) },
    )
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExperimentsScreenPreview() = ExperimentsScreen(
    state = CachedUiState.Content(
        listOf(
            TestExperiment,
            TestExperiment.copy(id = 4, name = "Onboarding copy", status = ExperimentStatus.Draft, startDate = null),
        ),
        isRefreshing = false,
    ),
    onNavigateBack = {},
    onRefresh = {},
    onOpenExperiment = {},
    onOpenInPostHog = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExperimentsScreenEmptyPreview() = ExperimentsScreen(
    state = CachedUiState.Empty(isRefreshing = false),
    onNavigateBack = {},
    onRefresh = {},
    onOpenExperiment = {},
    onOpenInPostHog = {},
)
