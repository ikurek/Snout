package com.nightcode.snout.shared.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `created_by` style user reference returned by most PostHog resources. */
@Serializable
data class UserBasicDto(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("email") val email: String? = null,
) {
    val displayName: String
        get() = listOfNotNull(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { email.orEmpty() }
}
