package com.nightcode.snout.shared.domain.model.analytics

/** Relative date range as PostHog understands it, plus the interval that keeps charts readable. */
enum class AnalyticsRange(val dateFrom: String, val interval: String) {
    Today("dStart", "hour"),
    Last7Days("-7d", "day"),
    Last30Days("-30d", "day"),
    Last90Days("-90d", "week"),
}
