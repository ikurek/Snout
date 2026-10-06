package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val SelectedOrganizationIdKey = stringPreferencesKey("selected_organization_id")
private val SelectedProjectIdKey = longPreferencesKey("selected_project_id")
private val ThemeModeKey = stringPreferencesKey("theme_mode")
private val NavigationTabsKey = stringPreferencesKey("navigation_tabs")
private val NavigationLandingKey = stringPreferencesKey("navigation_landing")
private val AnalyticsDefaultRangeKey = stringPreferencesKey("analytics_default_range")
private val AnalyticsMetricsKey = stringPreferencesKey("analytics_metrics")
private val AnalyticsShowVisitorsTrendKey = booleanPreferencesKey("analytics_show_visitors_trend")
private val AnalyticsBreakdownsKey = stringPreferencesKey("analytics_breakdowns")

/** Raw bottom-navigation settings as stored; `null` means never saved. Lists are comma-separated names. */
data class StoredNavigationPreferences(val tabs: String?, val landing: String?)

/** Raw analytics settings as stored; `null` means never saved. Lists are comma-separated names. */
data class StoredAnalyticsPreferences(
    val defaultRange: String?,
    val metrics: String?,
    val showVisitorsTrend: Boolean?,
    val breakdowns: String?,
)

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

    fun observeThemeMode(): Flow<String?> = dataStore.data.map { it[ThemeModeKey] }

    suspend fun saveThemeMode(themeMode: String) {
        dataStore.edit { it[ThemeModeKey] = themeMode }
    }

    fun observeNavigationPreferences(): Flow<StoredNavigationPreferences> = dataStore.data.map {
        StoredNavigationPreferences(tabs = it[NavigationTabsKey], landing = it[NavigationLandingKey])
    }

    suspend fun saveNavigationPreferences(tabs: String, landing: String) {
        dataStore.edit {
            it[NavigationTabsKey] = tabs
            it[NavigationLandingKey] = landing
        }
    }

    fun observeAnalyticsPreferences(): Flow<StoredAnalyticsPreferences> = dataStore.data.map {
        StoredAnalyticsPreferences(
            defaultRange = it[AnalyticsDefaultRangeKey],
            metrics = it[AnalyticsMetricsKey],
            showVisitorsTrend = it[AnalyticsShowVisitorsTrendKey],
            breakdowns = it[AnalyticsBreakdownsKey],
        )
    }

    suspend fun saveAnalyticsPreferences(
        defaultRange: String,
        metrics: String,
        showVisitorsTrend: Boolean,
        breakdowns: String,
    ) {
        dataStore.edit {
            it[AnalyticsDefaultRangeKey] = defaultRange
            it[AnalyticsMetricsKey] = metrics
            it[AnalyticsShowVisitorsTrendKey] = showVisitorsTrend
            it[AnalyticsBreakdownsKey] = breakdowns
        }
    }

    suspend fun clearSelectedProject() {
        dataStore.edit {
            it.remove(SelectedOrganizationIdKey)
            it.remove(SelectedProjectIdKey)
        }
    }
}
