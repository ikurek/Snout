package com.nightcode.snout.common.format

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.month_april
import snout.composeapp.generated.resources.month_august
import snout.composeapp.generated.resources.month_december
import snout.composeapp.generated.resources.month_february
import snout.composeapp.generated.resources.month_january
import snout.composeapp.generated.resources.month_july
import snout.composeapp.generated.resources.month_june
import snout.composeapp.generated.resources.month_march
import snout.composeapp.generated.resources.month_may
import snout.composeapp.generated.resources.month_november
import snout.composeapp.generated.resources.month_october
import snout.composeapp.generated.resources.month_september
import kotlin.time.Instant

@Composable
fun Instant.formatMonthYear(): String {
    val monthNames = MonthNames(
        january = stringResource(Res.string.month_january),
        february = stringResource(Res.string.month_february),
        march = stringResource(Res.string.month_march),
        april = stringResource(Res.string.month_april),
        may = stringResource(Res.string.month_may),
        june = stringResource(Res.string.month_june),
        july = stringResource(Res.string.month_july),
        august = stringResource(Res.string.month_august),
        september = stringResource(Res.string.month_september),
        october = stringResource(Res.string.month_october),
        november = stringResource(Res.string.month_november),
        december = stringResource(Res.string.month_december),
    )

    return LocalDateTime.Format {
        monthName(monthNames)
        char(' ')
        year()
    }.format(toLocalDateTime(TimeZone.currentSystemDefault()))
}
