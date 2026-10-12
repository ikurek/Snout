package com.nightcode.snout.features.experiments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.molecules.CodeChip
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.experiments.components.ExperimentStatusPill
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.model.experiments.ExperimentStatus
import com.nightcode.snout.shared.domain.model.experiments.TestExperiment
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.experiment_not_started
import snout.composeapp.generated.resources.experiment_started

/** Section body only; the home shell or [com.nightcode.snout.features.home.HomeSectionRoute] supplies the chrome. */
@Composable
internal fun ExperimentsScreen(
    state: CachedUiState<List<Experiment>>,
    onRefresh: () -> Unit,
    onOpenExperiment: (experimentId: Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    CachedStateContainer(state = state, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) { experiments ->
        LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large) + contentPadding) {
            itemsIndexed(experiments, key = { _, experiment -> experiment.id }) { index, experiment ->
                ExperimentRow(
                    experiment = experiment,
                    divider = index < experiments.lastIndex,
                    onClick = { onOpenExperiment(experiment.id) },
                )
            }
        }
    }
}

@Composable
private fun ExperimentRow(experiment: Experiment, divider: Boolean, onClick: () -> Unit) {
    EntityRow(
        title = experiment.name,
        divider = divider,
        onClick = onClick,
        meta = {
            experiment.featureFlagKey?.let { CodeChip(text = it) }
            MetaText(
                text = experiment.startDate?.let { stringResource(Res.string.experiment_started, it.formatShortDate()) }
                    ?: stringResource(Res.string.experiment_not_started),
            )
        },
        trailing = { ExperimentStatusPill(status = experiment.status) },
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
    onRefresh = {},
    onOpenExperiment = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExperimentsScreenEmptyPreview() = ExperimentsScreen(
    state = CachedUiState.Empty(isRefreshing = false),
    onRefresh = {},
    onOpenExperiment = {},
)
