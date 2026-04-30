package com.nightcode.snout.shared.data.user

import com.nightcode.snout.shared.api.NetworkError
import com.nightcode.snout.shared.api.users.model.UserDto
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError
import com.nightcode.snout.shared.domain.model.user.User
import io.ktor.http.HttpStatusCode
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal fun UserDto.toUser(): User = User(
    dateJoined = dateJoined,
    uuid = Uuid.parse(uuid),
    firstName = firstName,
    lastName = lastName,
)

internal fun NetworkError.toGetCurrentUserError(): GetCurrentUserError = when (this) {
    is NetworkError.Api -> if (statusCode == HttpStatusCode.Unauthorized.value) {
        GetCurrentUserError.AuthorizationFailed
    } else {
        GetCurrentUserError.UnexpectedError
    }

    is NetworkError.Http -> if (statusCode == HttpStatusCode.Unauthorized.value) {
        GetCurrentUserError.AuthorizationFailed
    } else {
        GetCurrentUserError.UnexpectedError
    }

    is NetworkError.NoConnection -> GetCurrentUserError.ConnectionError

    is NetworkError.Unexpected -> GetCurrentUserError.UnexpectedError
}
