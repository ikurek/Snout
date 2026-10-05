package com.nightcode.snout.shared.api.activitylog

import com.nightcode.snout.shared.api.PaginatedResponseDto
import com.nightcode.snout.shared.api.activitylog.model.ActivityLogDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

private const val PageSize = 50

class ActivityLogApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    // ponytail: newest 50 entries only; cursor paging can follow `next` later.
    suspend fun getActivityLog(projectId: Long) = networkCall<PaginatedResponseDto<ActivityLogDto>> {
        client.get(authDataStore.projectUrl(projectId, "activity_log/")) { parameter("limit", PageSize) }.body()
    }
}
