package com.nightcode.snout.shared.api.users.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserDto(
    @SerialName("date_joined")
    val dateJoined: Instant,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("email")
    val email: String,
)
