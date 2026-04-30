package com.nightcode.snout.shared.data.user.mapper

import com.nightcode.snout.shared.api.users.model.UserDto
import com.nightcode.snout.shared.domain.model.user.User
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal fun UserDto.toUser(): User = User(
    dateJoined = dateJoined,
    uuid = Uuid.parse(uuid),
    firstName = firstName,
    lastName = lastName,
)
