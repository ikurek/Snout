package com.nightcode.snout.shared.data.appstate

import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.storage.datastore.StoredAnalyticsPreferences
import com.nightcode.snout.shared.storage.datastore.StoredNavigationPreferences

private const val Separator = ","

/**
 * Turns stored enum names back into preferences. Anything missing, unknown or inconsistent (for example a
 * section renamed in a newer build) falls back to the defaults so a stale store can never break navigation.
 */
internal object PreferencesCodec {

    fun decodeNavigation(stored: StoredNavigationPreferences): NavigationPreferences {
        val tabs = stored.tabs.decodeList<HomeSection>()
        val landing = stored.landing?.let { name -> HomeSection.entries.firstOrNull { it.name == name } }
        val candidate = NavigationPreferences(
            tabs = tabs,
            landing = landing ?: tabs.firstOrNull() ?: return defaultNav(),
        )
        return if (candidate.isValid) candidate else defaultNav()
    }

    fun encodeTabs(preferences: NavigationPreferences): String = preferences.tabs.joinToString(Separator) { it.name }

    fun decodeAnalytics(stored: StoredAnalyticsPreferences): AnalyticsPreferences {
        val defaults = AnalyticsPreferences.Default
        val hiddenMetrics = stored.hiddenMetrics.decodeList<OverviewMetricKey>().toSet()
        // Hiding every stat card is not allowed; a store that says so is treated as hiding none.
        val metrics = (defaults.metrics - hiddenMetrics).ifEmpty { defaults.metrics }
        return AnalyticsPreferences(
            defaultRange = stored.defaultRange?.let { name -> AnalyticsRange.entries.firstOrNull { it.name == name } }
                ?: defaults.defaultRange,
            metrics = metrics,
            showTrendChart = stored.showTrendChart ?: defaults.showTrendChart,
            breakdowns = defaults.breakdowns - stored.hiddenBreakdowns.decodeList<WebBreakdown>().toSet(),
        )
    }

    fun encodeHiddenMetrics(preferences: AnalyticsPreferences): String = AnalyticsPreferences.ConfigurableMetrics
        .filterNot { it in preferences.metrics }
        .joinToString(Separator) { it.name }

    fun encodeHiddenBreakdowns(preferences: AnalyticsPreferences): String =
        WebBreakdown.entries.filterNot { it in preferences.breakdowns }.joinToString(Separator) { it.name }

    private fun defaultNav() = NavigationPreferences.Default

    private inline fun <reified T : Enum<T>> String?.decodeList(): List<T> = this
        ?.split(Separator)
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.mapNotNull { name -> enumValues<T>().firstOrNull { it.name == name } }
        .orEmpty()
}
