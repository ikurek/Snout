package com.nightcode.snout.shared.domain.model.analytics

import kotlinx.serialization.Serializable

enum class WebBreakdown(val apiValue: String) {
    Page("Page"),
    ReferringDomain("InitialReferringDomain"),
    Country("Country"),
    DeviceType("DeviceType"),
}

@Serializable
data class BreakdownRow(val label: String, val visitors: Double, val views: Double?)

@Serializable
data class BreakdownTable(val breakdown: WebBreakdown, val rows: List<BreakdownRow>)
