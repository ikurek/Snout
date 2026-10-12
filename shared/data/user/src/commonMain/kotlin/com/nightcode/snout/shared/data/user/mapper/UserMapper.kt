package com.nightcode.snout.shared.data.user.mapper

import com.nightcode.snout.shared.api.users.model.UserDto
import com.nightcode.snout.shared.data.user.model.StoredUser
import com.nightcode.snout.shared.domain.model.user.User
import kotlin.uuid.Uuid

internal fun UserDto.toUser(): User = User(
    dateJoined = dateJoined,
    uuid = Uuid.parse(uuid),
    firstName = firstName,
    lastName = lastName,
    email = email,
)

internal fun User.toStoredUser(): StoredUser = StoredUser(
    dateJoined = dateJoined,
    uuid = uuid.toString(),
    firstName = firstName,
    lastName = lastName,
    email = email,
)

internal fun StoredUser.toUser(): User = User(
    dateJoined = dateJoined,
    uuid = Uuid.parse(uuid),
    firstName = firstName,
    lastName = lastName,
    email = email,
)
