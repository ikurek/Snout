package com.nightcode.snout.shared.data.organization.mapper

import com.nightcode.snout.shared.api.organizations.model.OrganizationMemberDto
import com.nightcode.snout.shared.data.common.nullIfBlank
import com.nightcode.snout.shared.domain.model.organization.OrganizationMember

/** Null for a member without a user id, who can't be assigned anything. */
internal fun OrganizationMemberDto.toMember(): OrganizationMember? {
    val userId = user.id ?: return null
    return OrganizationMember(
        userId = userId,
        name = user.displayName.nullIfBlank() ?: userId.toString(),
        email = user.email.nullIfBlank(),
    )
}
