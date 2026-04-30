package com.nightcode.snout.shared.api.organizations

import com.nightcode.snout.shared.api.PaginatedResponseDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.organizations.model.OrganizationDto
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class OrganizationsApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    suspend fun getOrganizations() = networkCall<PaginatedResponseDto<OrganizationDto>> {
        client.get("${authDataStore.getInstanceUrl()}/api/organizations/").body()
    }
}
