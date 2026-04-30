package com.nightcode.snout.shared.domain.model.organization

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Organization(
    val id: Uuid,
    val name: String,
    val slug: String,
    val logoUrl: String?,
    val projects: List<Project>,
)
