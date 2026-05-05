package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val SelectedOrganizationIdKey = stringPreferencesKey("selected_organization_id")
private val SelectedProjectIdKey = longPreferencesKey("selected_project_id")

class AppStateDataStore internal constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun getSelectedOrganizationId(): String? = dataStore.data.map { it[SelectedOrganizationIdKey] }
        .firstOrNull()
        ?.takeIf { it.isNotBlank() }

    suspend fun getSelectedProjectId(): Long? = dataStore.data.map { it[SelectedProjectIdKey] }.firstOrNull()

    fun observeSelectedProject(): Flow<Pair<String?, Long?>> = dataStore.data.map { prefs ->
        val orgId = prefs[SelectedOrganizationIdKey]?.takeIf { it.isNotBlank() }
        val projectId = prefs[SelectedProjectIdKey]
        orgId to projectId
    }

    suspend fun saveSelectedProject(organizationId: String, projectId: Long) {
        dataStore.edit {
            it[SelectedOrganizationIdKey] = organizationId
            it[SelectedProjectIdKey] = projectId
        }
    }
}
