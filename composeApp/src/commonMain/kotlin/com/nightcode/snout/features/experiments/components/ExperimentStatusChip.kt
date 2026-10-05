package com.nightcode.snout.features.experiments.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.designsystem.atoms.LabelChip
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.experiments.ExperimentStatus
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.experiment_status_draft
import snout.composeapp.generated.resources.experiment_status_exposure_frozen
import snout.composeapp.generated.resources.experiment_status_paused
import snout.composeapp.generated.resources.experiment_status_running
import snout.composeapp.generated.resources.experiment_status_stopped
import snout.composeapp.generated.resources.experiment_status_unknown

@Composable
internal fun ExperimentStatusChip(status: ExperimentStatus) {
    val extended = SnoutTheme.extendedColors
    val neutral = SnoutTheme.colors.surfaceContainerHighest to SnoutTheme.colors.onSurfaceVariant
    val (label, colors) = when (status) {
        ExperimentStatus.Draft -> Res.string.experiment_status_draft to neutral

        ExperimentStatus.Running ->
            Res.string.experiment_status_running to (extended.successContainer to extended.onSuccessContainer)

        ExperimentStatus.Paused ->
            Res.string.experiment_status_paused to (extended.warningContainer to SnoutTheme.colors.onSurface)

        ExperimentStatus.ExposureFrozen ->
            Res.string.experiment_status_exposure_frozen to (extended.warningContainer to SnoutTheme.colors.onSurface)

        ExperimentStatus.Stopped -> Res.string.experiment_status_stopped to neutral

        ExperimentStatus.Unknown -> Res.string.experiment_status_unknown to neutral
    }
    LabelChip(text = stringResource(label), containerColor = colors.first, contentColor = colors.second)
}
