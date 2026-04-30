package com.nightcode.snout.shared.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiErrorDto(
    @SerialName("type")
    val type: String,
    @SerialName("code")
    val code: String,
    @SerialName("detail")
    val detail: String,
    @SerialName("attr")
    val attr: String?,
)
