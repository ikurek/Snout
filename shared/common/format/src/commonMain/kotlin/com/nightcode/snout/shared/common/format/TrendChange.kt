package com.nightcode.snout.shared.common.format

import kotlin.math.abs

/** Below this, in either direction, a change counts as none. */
private const val FlatThresholdPercent = 0.05

/** Whether a change is good news. */
enum class ChangeTone { Good, Bad, Neutral }

/**
 * A period-over-period change as the app's trend chips and the widgets write it ("↑ 12.8%"): the arrow is always
 * there, so colour is never the only signal.
 */
data class TrendChange(val text: String, val tone: ChangeTone)

/** [changePercent] as a [TrendChange]; [lowerIsBetter] flips which direction counts as good (bounce rate, errors). */
fun trendChange(changePercent: Double, lowerIsBetter: Boolean = false): TrendChange {
    val flat = abs(changePercent) < FlatThresholdPercent
    val improved = if (lowerIsBetter) changePercent < 0 else changePercent > 0
    val arrow = when {
        flat -> "→"
        changePercent > 0 -> "↑"
        else -> "↓"
    }
    val tone = when {
        flat -> ChangeTone.Neutral
        improved -> ChangeTone.Good
        else -> ChangeTone.Bad
    }
    return TrendChange(text = "$arrow ${abs(changePercent).formatDecimals(1)}%", tone = tone)
}
