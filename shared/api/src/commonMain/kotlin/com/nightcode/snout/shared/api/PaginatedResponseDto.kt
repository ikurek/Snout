package com.nightcode.snout.shared.api

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponseDto<T>(val results: List<T>)
