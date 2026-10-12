package com.nightcode.snout.shared.data.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.storage.datastore.AppStateDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single(binds = [AppStateRepository::class])
internal class AppStateRepositoryImpl(private val dataStore: AppStateDataStore) : AppStateRepository {

    override suspend fun saveSelectedProject(organizationId: Uuid, projectId: Long) =
        dataStore.saveSelectedProject(organizationId.toString(), projectId)

    override suspend fun getSelectedProject(): SelectedProject? =
        selectedProject(dataStore.getSelectedOrganizationId(), dataStore.getSelectedProjectId())

    override fun observeSelectedProject(): Flow<SelectedProject?> =
        dataStore.observeSelectedProject().map { (organizationId, projectId) ->
            selectedProject(organizationId, projectId)
        }

    override suspend fun clearSelectedProject() = dataStore.clearSelectedProject()

    override fun observeThemeMode(): Flow<ThemeMode> = dataStore.observeThemeMode().map { stored ->
        ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.System
    }

    override suspend fun saveThemeMode(themeMode: ThemeMode) = dataStore.saveThemeMode(themeMode.name)

    override fun observeNavigationPreferences(): Flow<NavigationPreferences> =
        dataStore.observeNavigationPreferences().map(PreferencesCodec::decodeNavigation)

    override suspend fun saveNavigationPreferences(preferences: NavigationPreferences) =
        dataStore.saveNavigationPreferences(
            tabs = PreferencesCodec.encodeTabs(preferences),
            landing = preferences.landing.name,
        )

    override fun observeAnalyticsPreferences(): Flow<AnalyticsPreferences> =
        dataStore.observeAnalyticsPreferences().map(PreferencesCodec::decodeAnalytics)

    override suspend fun saveAnalyticsPreferences(preferences: AnalyticsPreferences) =
        dataStore.saveAnalyticsPreferences(
            defaultRange = preferences.defaultRange.name,
            hiddenMetrics = PreferencesCodec.encodeHiddenMetrics(preferences),
            showTrendChart = preferences.showTrendChart,
            hiddenBreakdowns = PreferencesCodec.encodeHiddenBreakdowns(preferences),
        )

    private fun selectedProject(organizationId: String?, projectId: Long?): SelectedProject? =
        if (organizationId == null || projectId == null) {
            null
        } else {
            SelectedProject(organizationId = Uuid.parse(organizationId), projectId = projectId)
        }
}
