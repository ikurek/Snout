package com.nightcode.snout.shared.domain.model.organization

import com.nightcode.snout.shared.common.testdata.TestData

data class OrganizationMember(
    /** The numeric user id PostHog refers to them by elsewhere. */
    val userId: Long,
    /** Their full name, else their email. */
    val name: String,
    val email: String?,
)

@TestData
val TestOrganizationMembers = listOf(
    OrganizationMember(userId = 7, name = "Ada Lovelace", email = "ada@example.com"),
    OrganizationMember(userId = 9, name = "Grace Hopper", email = "grace@example.com"),
)
