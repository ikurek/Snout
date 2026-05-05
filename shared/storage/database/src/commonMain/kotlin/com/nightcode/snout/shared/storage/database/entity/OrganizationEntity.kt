package com.nightcode.snout.shared.storage.database.entity

data class OrganizationEntity(
    val id: String,
    val name: String,
    val slug: String,
    val logoUrl: String?,
    val projects: List<ProjectEntity>,
)
