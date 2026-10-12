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
import org.koin.core.annotation.Single

private const val PageSize = 50

@Single
class ActivityLogApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    // Limitation: newest 50 entries only; cursor paging would follow the response's `next` link.
    suspend fun getActivityLog(projectId: Long) = networkCall<PaginatedResponseDto<ActivityLogDto>> {
        client.get(authDataStore.projectUrl(projectId, "activity_log/")) { parameter("limit", PageSize) }.body()
    }
}
