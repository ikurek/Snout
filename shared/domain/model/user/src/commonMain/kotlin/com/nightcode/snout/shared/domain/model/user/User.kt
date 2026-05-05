package com.nightcode.snout.shared.domain.model.user

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class User(
    val dateJoined: Instant,
    val uuid: Uuid,
    val firstName: String,
    val lastName: String
)

@TestData
@OptIn(ExperimentalUuidApi::class)
val TestUser = User(
    dateJoined = Instant.parse("2024-01-01T00:00:00Z"),
    uuid = Uuid.parse("00000000-0000-0000-0000-000000000002"),
    firstName = "John",
    lastName = "Doe",
)
