package com.nightcode.snout.features.experiments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.designsystem.molecules.CodeChip
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.RefreshableContent
import com.nightcode.snout.designsystem.organisms.RefreshableEmptyContent
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
    state: ExperimentsUiState,
    onRefresh: () -> Unit,
    onOpenExperiment: (experimentId: Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val experiments = state.experiments
    when {
        experiments == null -> LoadingOrErrorContent(error = state.error, onRetry = onRefresh)

        experiments.isEmpty() -> RefreshableEmptyContent(isRefreshing = state.isRefreshing, onRefresh = onRefresh)

        else -> RefreshableContent(
            isRefreshing = state.isRefreshing,
            refreshError = state.error,
            onRefresh = onRefresh,
        ) {
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
    state = ExperimentsUiState(
        experiments = listOf(
            TestExperiment,
            TestExperiment.copy(id = 4, name = "Onboarding copy", status = ExperimentStatus.Draft, startDate = null),
        ),
    ),
    onRefresh = {},
    onOpenExperiment = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ExperimentsScreenEmptyPreview() = ExperimentsScreen(
    state = ExperimentsUiState(experiments = emptyList()),
    onRefresh = {},
    onOpenExperiment = {},
)
