package com.nightcode.snout.shared.api.annotations

import com.nightcode.snout.shared.api.PaginatedResponseDto
import com.nightcode.snout.shared.api.annotations.model.AnnotationDto
import com.nightcode.snout.shared.api.annotations.model.CreateAnnotationDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.postJson
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

private const val PageSize = 100

class AnnotationsApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    suspend fun getAnnotations(projectId: Long) = networkCall<PaginatedResponseDto<AnnotationDto>> {
        client.get(authDataStore.projectUrl(projectId, "annotations/")) { parameter("limit", PageSize) }.body()
    }

    /** Requires the `annotation:write` scope. */
    suspend fun createAnnotation(projectId: Long, body: CreateAnnotationDto) = networkCall<AnnotationDto> {
        client.postJson(authDataStore.projectUrl(projectId, "annotations/"), body).body()
    }
}
