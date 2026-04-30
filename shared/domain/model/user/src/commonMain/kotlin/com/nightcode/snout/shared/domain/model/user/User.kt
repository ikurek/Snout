package com.nightcode.snout.shared.domain.model.user

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class User(val dateJoined: Instant, val uuid: Uuid, val firstName: String, val lastName: String)
