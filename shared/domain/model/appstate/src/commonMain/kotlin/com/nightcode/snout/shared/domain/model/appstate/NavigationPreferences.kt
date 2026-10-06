package com.nightcode.snout.shared.domain.model.appstate

/**
 * Which sections fill the four configurable bottom-bar slots, in order, and which one the app opens on.
 * The fifth slot is always More and lists every section not in [tabs]. [landing] is always one of [tabs].
 */
data class NavigationPreferences(val tabs: List<HomeSection>, val landing: HomeSection) {

    val hidden: List<HomeSection> get() = HomeSection.entries.filterNot { it in tabs }

    val isValid: Boolean
        get() = tabs.size == TabCount && tabs.distinct().size == TabCount && landing in tabs

    /** Puts [section] into [slot]; if it already sat in another slot the two sections swap places. */
    fun withTab(slot: Int, section: HomeSection): NavigationPreferences {
        require(slot in tabs.indices) { "Slot $slot out of range" }
        val updated = tabs.toMutableList()
        val previousSlot = tabs.indexOf(section)
        if (previousSlot >= 0) updated[previousSlot] = tabs[slot]
        updated[slot] = section
        return copy(tabs = updated).withLandingInTabs()
    }

    fun withLanding(section: HomeSection): NavigationPreferences = copy(landing = section).withLandingInTabs()

    private fun withLandingInTabs(): NavigationPreferences = if (landing in tabs) this else copy(landing = tabs.first())

    companion object {
        const val TabCount = 4

        val Default = NavigationPreferences(
            tabs = listOf(
                HomeSection.Analytics,
                HomeSection.Dashboards,
                HomeSection.Insights,
                HomeSection.FeatureFlags,
            ),
            landing = HomeSection.Analytics,
        )
    }
}
