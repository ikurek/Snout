package com.nightcode.snout.shared.api

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponseDto<T>(val count: Int, val results: List<T>)
