package com.nightcode.snout.shared.domain.data.appstate

import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface AppStateRepository {
    suspend fun saveSelectedProject(organizationId: Uuid, projectId: Long)
    suspend fun getSelectedProject(): SelectedProject?
    fun observeSelectedProject(): Flow<SelectedProject?>
    suspend fun clearSelectedProject()
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun saveThemeMode(themeMode: ThemeMode)
    fun observeNavigationPreferences(): Flow<NavigationPreferences>
    suspend fun saveNavigationPreferences(preferences: NavigationPreferences)
    fun observeAnalyticsPreferences(): Flow<AnalyticsPreferences>
    suspend fun saveAnalyticsPreferences(preferences: AnalyticsPreferences)
}
