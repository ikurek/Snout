package com.nightcode.snout.features.experiments.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.designsystem.atoms.StatusPill
import com.nightcode.snout.designsystem.atoms.StatusTone
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
internal fun ExperimentStatusPill(status: ExperimentStatus) {
    val (label, tone) = when (status) {
        ExperimentStatus.Draft -> Res.string.experiment_status_draft to StatusTone.Neutral
        ExperimentStatus.Running -> Res.string.experiment_status_running to StatusTone.Active
        ExperimentStatus.Paused -> Res.string.experiment_status_paused to StatusTone.Attention
        ExperimentStatus.ExposureFrozen -> Res.string.experiment_status_exposure_frozen to StatusTone.Attention
        ExperimentStatus.Stopped -> Res.string.experiment_status_stopped to StatusTone.Info
        ExperimentStatus.Unknown -> Res.string.experiment_status_unknown to StatusTone.Neutral
    }
    StatusPill(text = stringResource(label), tone = tone)
}
