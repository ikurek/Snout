package com.nightcode.snout.shared.data.appstate

import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.storage.datastore.AppStateDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

internal class AppStateRepositoryImpl(private val dataStore: AppStateDataStore) : AppStateRepository {

    override suspend fun saveSelectedProject(organizationId: Uuid, projectId: Long) =
        dataStore.saveSelectedProject(organizationId.toString(), projectId)

    override suspend fun getSelectedProject(): SelectedProject? = either {
        val organizationId = ensureNotNull(dataStore.getSelectedOrganizationId()) {}
        val projectId = ensureNotNull(dataStore.getSelectedProjectId()) {}
        SelectedProject(organizationId = Uuid.parse(organizationId), projectId = projectId)
    }.getOrNull()

    override fun observeSelectedProject(): Flow<SelectedProject?> =
        dataStore.observeSelectedProject().map { (orgIdStr, projectId) ->
            if (orgIdStr == null || projectId == null) {
                null
            } else {
                SelectedProject(organizationId = Uuid.parse(orgIdStr), projectId = projectId)
            }
        }

    override suspend fun clearSelectedProject() = dataStore.clearSelectedProject()

    override fun observeThemeMode(): Flow<ThemeMode> = dataStore.observeThemeMode().map { stored ->
        ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.System
    }

    override suspend fun saveThemeMode(themeMode: ThemeMode) = dataStore.saveThemeMode(themeMode.name)
}
