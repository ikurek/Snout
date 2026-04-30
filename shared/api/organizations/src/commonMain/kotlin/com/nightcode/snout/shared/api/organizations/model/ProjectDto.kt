package com.nightcode.snout.shared.api.organizations.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(@SerialName("id") val id: Long, @SerialName("name") val name: String)
