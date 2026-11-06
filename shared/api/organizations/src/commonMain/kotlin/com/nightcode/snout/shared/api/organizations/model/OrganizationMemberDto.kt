package com.nightcode.snout.shared.api.organizations.model

import com.nightcode.snout.shared.api.UserBasicDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationMemberDto(@SerialName("user") val user: UserBasicDto)
