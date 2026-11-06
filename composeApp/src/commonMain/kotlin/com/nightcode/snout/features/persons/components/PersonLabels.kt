package com.nightcode.snout.features.persons.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.shared.domain.model.persons.Person
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.persons_created
import kotlin.time.Instant

internal val Person.secondaryLabel: String?
    get() = listOfNotNull(properties["name"], email, distinctIds.firstOrNull()).firstOrNull { it != displayName }

@Composable
internal fun PersonCreatedMeta(createdAt: Instant) {
    MetaText(stringResource(Res.string.persons_created, createdAt.formatRelative()))
}
