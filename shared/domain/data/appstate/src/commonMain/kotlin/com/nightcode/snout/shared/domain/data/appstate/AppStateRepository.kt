package com.nightcode.snout.shared.domain.data.appstate

import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface AppStateRepository {
    suspend fun saveSelectedProject(organizationId: Uuid, projectId: Long)
    suspend fun getSelectedProject(): SelectedProject?
    fun observeSelectedProject(): Flow<SelectedProject?>
}
