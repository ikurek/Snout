package com.nightcode.snout.shared.common.format

import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind

/** A web analytics stat as the Analytics tab and the widgets show it: 12.4k visitors, 2m 23s, 42%. */
fun OverviewMetricKind.format(value: Double): String = when (this) {
    OverviewMetricKind.Count -> value.formatCompact()
    OverviewMetricKind.DurationSeconds -> value.formatDurationSeconds()
    OverviewMetricKind.Percentage -> value.formatPercent(isFraction = value <= 1.0)
}

/** Where a currency's sign goes before the amount; the others follow it as their code. */
private val CurrencySigns = mapOf("USD" to "$", "EUR" to "€", "GBP" to "£")

/** [amount] in [currency] (an ISO code), compact as the stats are: $12.4k, €980, 1.2k PLN. */
fun formatMoney(amount: Double, currency: String): String =
    CurrencySigns[currency]?.let { "$it${amount.formatCompact()}" } ?: "${amount.formatCompact()} $currency"
