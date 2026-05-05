package com.nightcode.snout.shared.storage.database

import com.nightcode.snout.shared.storage.database.entity.OrganizationEntity
import kotlinx.coroutines.flow.Flow

interface OrganizationsDatabase {
    fun observe(): Flow<List<OrganizationEntity>>
    suspend fun replaceAll(organizations: List<OrganizationEntity>)
}
