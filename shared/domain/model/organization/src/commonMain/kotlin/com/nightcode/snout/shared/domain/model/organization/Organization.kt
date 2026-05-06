package com.nightcode.snout.shared.domain.model.organization

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.uuid.Uuid

data class Organization(
    val id: Uuid,
    val name: String,
    val slug: String,
    val logoUrl: String?,
    val projects: List<Project>,
)

@TestData
@OptIn(TestData::class)
val TestOrganization = Organization(
    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
    name = "Nightcode",
    slug = "nightcode",
    logoUrl = null,
    projects = listOf(TestProject),
)
