package com.nightcode.snout.shared.domain.model.analytics

/** Where a breakdown's numbers come from: PostHog's web analytics tables, or a trends query over an event property. */
enum class BreakdownSource {
    WebStats,
    EventProperty,
}

/**
 * Top-list tables on the Analytics tab. [apiValue] is PostHog's `breakdownBy` name for web stats tables, or the
 * event property to break down by. Order here is display order.
 */
enum class WebBreakdown(val apiValue: String, val source: BreakdownSource = BreakdownSource.WebStats) {
    Page("Page"),
    Screen("ScreenName"),
    EntryPage("InitialPage"),
    ExitPage("ExitPage"),
    Channel("InitialChannelType"),
    ReferringDomain("InitialReferringDomain"),
    UtmCampaign("InitialUTMCampaign"),
    Country("Country"),
    DeviceType("DeviceType"),
    Browser("Browser"),
    OperatingSystem("OS"),
    AppVersion("\$app_version", BreakdownSource.EventProperty),
}

data class BreakdownRow(val label: String, val visitors: Double, val views: Double?)

data class BreakdownTable(val breakdown: WebBreakdown, val rows: List<BreakdownRow>)
