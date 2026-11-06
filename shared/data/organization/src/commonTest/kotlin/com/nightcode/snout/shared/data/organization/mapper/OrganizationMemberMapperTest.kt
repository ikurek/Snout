package com.nightcode.snout.shared.data.organization.mapper

import com.nightcode.snout.shared.api.organizations.model.OrganizationMemberDto
import com.nightcode.snout.shared.data.testutils.TestJson
import com.nightcode.snout.shared.domain.model.organization.OrganizationMember
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class OrganizationMemberMapperTest :
    ShouldSpec({
        fun member(user: String) = TestJson.decodeFromString<OrganizationMemberDto>("""{"user":$user}""").toMember()

        should("name a member by their full name") {
            member("""{"id":7,"first_name":"Ada","last_name":"Lovelace","email":"ada@example.com"}""") shouldBe
                OrganizationMember(userId = 7, name = "Ada Lovelace", email = "ada@example.com")
        }

        should("fall back to the email, then the user id") {
            member("""{"id":7,"first_name":"","email":"ada@example.com"}""")?.name shouldBe "ada@example.com"
            member("""{"id":8}""")?.name shouldBe "8"
        }

        should("skip a member without a user id") {
            member("""{"first_name":"Ada"}""").shouldBeNull()
        }
    })
