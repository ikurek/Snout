package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.domain.model.insights.InsightDateRange
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightPeriod
import com.nightcode.snout.shared.domain.model.insights.PeriodUnit
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/** PostHog's default when a query has no date range. */
private const val DefaultDays = 7
private const val IsoDateLength = 10

// Case matters: PostHog reads "m" as months and "M" as minutes.
private val RelativeDate = Regex("""-(\d+)([hdwmy])""")
private val IsoDate = Regex("""\d{4}-\d{2}-\d{2}.*""")

private val NamedRanges: Map<String, InsightDateRange> = mapOf(
    "dStart" to InsightDateRange.Today,
    "mStart" to InsightDateRange.MonthToDate,
    "yStart" to InsightDateRange.YearToDate,
)

/** The dates and bucketing an insight's query covers; null for SQL, or a date filter Snout can't put into words. */
internal fun JsonElement?.insightPeriod(kind: InsightKind): InsightPeriod? {
    val source = sourceNode()
    val dateRange = source?.get("dateRange") as? JsonObject
    val from = dateRange?.stringValue("date_from")
    val range = when {
        source == null || kind == InsightKind.Sql || kind == InsightKind.Unknown -> null

        // Retention without a range follows its interval count rather than PostHog's 7 day default.
        from == null -> if (kind == InsightKind.Retention) null else InsightDateRange.Last(DefaultDays, PeriodUnit.Day)

        else -> dateRange(from, dateRange.stringValue("date_to"))
    }
    val bucketed = kind == InsightKind.Trends || kind == InsightKind.Lifecycle
    return range?.let {
        InsightPeriod(
            range = it,
            interval = if (bucketed) source?.stringValue("interval").toPeriodUnit() ?: PeriodUnit.Day else null,
            compared = ((source?.get("compareFilter") as? JsonObject)?.get("compare") as? JsonPrimitive)
                ?.booleanOrNull == true,
        )
    }
}

private fun dateRange(from: String, to: String?): InsightDateRange? = when {
    from == "all" -> InsightDateRange.AllTime

    from == "-1dStart" && to == "-1dEnd" -> InsightDateRange.Yesterday

    IsoDate.matches(from) -> if (to == null || IsoDate.matches(to)) {
        InsightDateRange.Between(from.take(IsoDateLength), to?.take(IsoDateLength))
    } else {
        null
    }

    // A relative start with an explicit end ("-30d" to "-7d") has no short wording; leave it out.
    to != null -> null

    else -> NamedRanges[from] ?: relativeRange(from)
}

private fun relativeRange(from: String): InsightDateRange? = RelativeDate.matchEntire(from)?.let { match ->
    match.groupValues[2].toPeriodUnit()?.let { InsightDateRange.Last(match.groupValues[1].toInt(), it) }
}

private fun String?.toPeriodUnit(): PeriodUnit? = when (this) {
    "h", "hour" -> PeriodUnit.Hour
    "d", "day" -> PeriodUnit.Day
    "w", "week" -> PeriodUnit.Week
    "m", "month" -> PeriodUnit.Month
    "y", "year" -> PeriodUnit.Year
    else -> null
}
