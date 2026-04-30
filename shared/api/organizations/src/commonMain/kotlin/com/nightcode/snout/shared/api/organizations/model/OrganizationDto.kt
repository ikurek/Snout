package com.nightcode.snout.shared.api.organizations.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("logo_media_id") val logoMediaId: String?,
    @SerialName("projects") val projects: List<ProjectDto>,
)
