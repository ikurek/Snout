package com.nightcode.snout.features.insights.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.shared.domain.model.insights.InsightDateRange
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightPeriod
import com.nightcode.snout.shared.domain.model.insights.PeriodUnit
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_kind_funnels
import snout.composeapp.generated.resources.insight_kind_lifecycle
import snout.composeapp.generated.resources.insight_kind_other
import snout.composeapp.generated.resources.insight_kind_paths
import snout.composeapp.generated.resources.insight_kind_retention
import snout.composeapp.generated.resources.insight_kind_sql
import snout.composeapp.generated.resources.insight_kind_stickiness
import snout.composeapp.generated.resources.insight_kind_trends
import snout.composeapp.generated.resources.insight_period_all_time
import snout.composeapp.generated.resources.insight_period_between
import snout.composeapp.generated.resources.insight_period_compared
import snout.composeapp.generated.resources.insight_period_daily
import snout.composeapp.generated.resources.insight_period_hourly
import snout.composeapp.generated.resources.insight_period_last_days
import snout.composeapp.generated.resources.insight_period_last_hours
import snout.composeapp.generated.resources.insight_period_last_months
import snout.composeapp.generated.resources.insight_period_last_weeks
import snout.composeapp.generated.resources.insight_period_last_years
import snout.composeapp.generated.resources.insight_period_month_to_date
import snout.composeapp.generated.resources.insight_period_monthly
import snout.composeapp.generated.resources.insight_period_since
import snout.composeapp.generated.resources.insight_period_today
import snout.composeapp.generated.resources.insight_period_weekly
import snout.composeapp.generated.resources.insight_period_year_to_date
import snout.composeapp.generated.resources.insight_period_yearly
import snout.composeapp.generated.resources.insight_period_yesterday

internal val InsightKind.label: StringResource
    get() = when (this) {
        InsightKind.Trends -> Res.string.insight_kind_trends
        InsightKind.Funnels -> Res.string.insight_kind_funnels
        InsightKind.Retention -> Res.string.insight_kind_retention
        InsightKind.Paths -> Res.string.insight_kind_paths
        InsightKind.Stickiness -> Res.string.insight_kind_stickiness
        InsightKind.Lifecycle -> Res.string.insight_kind_lifecycle
        InsightKind.Sql -> Res.string.insight_kind_sql
        InsightKind.Unknown -> Res.string.insight_kind_other
    }

/** "Last 14 days · daily · compared to previous period" */
@Composable
internal fun InsightPeriod.label(): String = listOfNotNull(
    range.label(),
    interval?.let { stringResource(it.intervalLabel()) },
    if (compared) stringResource(Res.string.insight_period_compared) else null,
).joinToString(" · ")

@Composable
private fun InsightDateRange.label(): String = when (this) {
    is InsightDateRange.Last -> pluralStringResource(unit.lastLabel(), amount, amount)

    InsightDateRange.Today -> stringResource(Res.string.insight_period_today)

    InsightDateRange.Yesterday -> stringResource(Res.string.insight_period_yesterday)

    InsightDateRange.MonthToDate -> stringResource(Res.string.insight_period_month_to_date)

    InsightDateRange.YearToDate -> stringResource(Res.string.insight_period_year_to_date)

    InsightDateRange.AllTime -> stringResource(Res.string.insight_period_all_time)

    is InsightDateRange.Between -> {
        val start = from.dateLabel()
        val end = to?.dateLabel()
        if (end == null) {
            stringResource(Res.string.insight_period_since, start)
        } else {
            stringResource(Res.string.insight_period_between, start, end)
        }
    }
}

/** Formats an ISO date, falling back to the text PostHog sent when it isn't one. */
@Composable
private fun String.dateLabel(): String = runCatching { LocalDate.parse(this) }.getOrNull()?.formatShortDate() ?: this

private fun PeriodUnit.lastLabel() = when (this) {
    PeriodUnit.Hour -> Res.plurals.insight_period_last_hours
    PeriodUnit.Day -> Res.plurals.insight_period_last_days
    PeriodUnit.Week -> Res.plurals.insight_period_last_weeks
    PeriodUnit.Month -> Res.plurals.insight_period_last_months
    PeriodUnit.Year -> Res.plurals.insight_period_last_years
}

private fun PeriodUnit.intervalLabel() = when (this) {
    PeriodUnit.Hour -> Res.string.insight_period_hourly
    PeriodUnit.Day -> Res.string.insight_period_daily
    PeriodUnit.Week -> Res.string.insight_period_weekly
    PeriodUnit.Month -> Res.string.insight_period_monthly
    PeriodUnit.Year -> Res.string.insight_period_yearly
}
