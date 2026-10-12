package com.nightcode.snout.shared.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponseDto<T>(@SerialName("results") val results: List<T>)
