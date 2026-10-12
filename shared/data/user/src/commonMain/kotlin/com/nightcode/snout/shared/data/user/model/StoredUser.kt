package com.nightcode.snout.shared.data.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** The signed-in account as kept in the secure store; its own format, so the domain model can change freely. */
@Serializable
internal data class StoredUser(
    @SerialName("date_joined") val dateJoined: Instant,
    @SerialName("uuid") val uuid: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("email") val email: String,
)
