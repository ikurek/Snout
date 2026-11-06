package com.nightcode.snout.features.persons.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.features.persons.detail.PersonTab
import com.nightcode.snout.shared.domain.model.persons.PersonFlag
import com.nightcode.snout.shared.domain.model.persons.PersonFlagReason
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.person_flag_reason_condition
import snout.composeapp.generated.resources.person_flag_reason_condition_any
import snout.composeapp.generated.resources.person_flag_reason_disabled
import snout.composeapp.generated.resources.person_flag_reason_no_condition
import snout.composeapp.generated.resources.person_flag_reason_no_group
import snout.composeapp.generated.resources.person_flag_reason_out_of_rollout
import snout.composeapp.generated.resources.person_flag_reason_unknown
import snout.composeapp.generated.resources.person_tab_errors
import snout.composeapp.generated.resources.person_tab_events
import snout.composeapp.generated.resources.person_tab_flags
import snout.composeapp.generated.resources.person_tab_properties

internal val PersonTab.label: StringResource
    get() = when (this) {
        PersonTab.Properties -> Res.string.person_tab_properties
        PersonTab.Events -> Res.string.person_tab_events
        PersonTab.Flags -> Res.string.person_tab_flags
        PersonTab.Errors -> Res.string.person_tab_errors
    }

@Composable
internal fun PersonFlag.reasonText(): String {
    val condition = conditionIndex
    return when (reason) {
        PersonFlagReason.ConditionMatch if condition != null ->
            stringResource(Res.string.person_flag_reason_condition, condition)

        PersonFlagReason.ConditionMatch -> stringResource(Res.string.person_flag_reason_condition_any)

        PersonFlagReason.NoConditionMatch -> stringResource(Res.string.person_flag_reason_no_condition)

        PersonFlagReason.OutOfRollout -> stringResource(Res.string.person_flag_reason_out_of_rollout)

        PersonFlagReason.FlagDisabled -> stringResource(Res.string.person_flag_reason_disabled)

        PersonFlagReason.NoGroup -> stringResource(Res.string.person_flag_reason_no_group)

        PersonFlagReason.Unknown -> stringResource(Res.string.person_flag_reason_unknown)
    }
}
