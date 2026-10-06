package com.nightcode.snout.features.experiments.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.designsystem.LabelChip
import com.nightcode.snout.designsystem.LabelChipTone
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
    val (label, tone) = when (status) {
        ExperimentStatus.Draft -> Res.string.experiment_status_draft to LabelChipTone.Neutral
        ExperimentStatus.Running -> Res.string.experiment_status_running to LabelChipTone.Success
        ExperimentStatus.Paused -> Res.string.experiment_status_paused to LabelChipTone.Warning
        ExperimentStatus.ExposureFrozen -> Res.string.experiment_status_exposure_frozen to LabelChipTone.Warning
        ExperimentStatus.Stopped -> Res.string.experiment_status_stopped to LabelChipTone.Info
        ExperimentStatus.Unknown -> Res.string.experiment_status_unknown to LabelChipTone.Neutral
    }
    LabelChip(text = stringResource(label), tone = tone)
}
