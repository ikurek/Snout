package com.nightcode.snout.features.persons.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.common.format.formatLogTime
import com.nightcode.snout.designsystem.atoms.StatusPill
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.persons.PersonEvent
import com.nightcode.snout.shared.domain.model.persons.PersonException
import com.nightcode.snout.shared.domain.model.persons.PersonFlag
import com.nightcode.snout.shared.domain.model.persons.PersonFlagValue
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.person_errors_unnamed
import snout.composeapp.generated.resources.person_flag_off
import snout.composeapp.generated.resources.person_flag_on

@Composable
internal fun PersonEventRow(event: PersonEvent, divider: Boolean) {
    EntityRow(
        title = event.name,
        titleStyle = SnoutTheme.code.medium,
        titleMaxLines = 1,
        subtitle = event.location,
        divider = divider,
        meta = { MetaText(event.timestamp.formatLogTime()) },
    )
}

@Composable
internal fun PersonFlagRow(flag: PersonFlag, divider: Boolean) {
    EntityRow(
        title = flag.key,
        titleStyle = SnoutTheme.code.medium,
        titleMaxLines = 1,
        divider = divider,
        meta = { MetaText(flag.reasonText()) },
        trailing = {
            when (val value = flag.value) {
                PersonFlagValue.Enabled -> StatusPill(
                    stringResource(Res.string.person_flag_on),
                    tone = StatusTone.Active,
                )

                PersonFlagValue.Disabled -> StatusPill(stringResource(Res.string.person_flag_off))

                is PersonFlagValue.Variant -> Tag(text = value.key, tone = StatusTone.Info)
            }
        },
    )
}

@Composable
internal fun PersonExceptionRow(exception: PersonException, divider: Boolean, onClick: (() -> Unit)?) {
    EntityRow(
        title = exception.type ?: exception.message ?: stringResource(Res.string.person_errors_unnamed),
        titleMaxLines = 1,
        subtitle = exception.message.takeIf { exception.type != null },
        divider = divider,
        onClick = onClick,
        meta = { MetaText(exception.timestamp.formatLogTime()) },
    )
}
