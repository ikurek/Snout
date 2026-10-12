package com.nightcode.snout.shared.domain.model.insights

import com.nightcode.snout.shared.common.testdata.TestData
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

private fun series(vararg values: Double, aggregatedValue: Double? = null, previous: TrendSeries? = null) = TrendSeries(
    label = "\$pageview",
    values = values.toList(),
    periods = values.indices.map { "2026-10-0${it + 1}" },
    aggregatedValue = aggregatedValue,
    previous = previous,
)

@OptIn(TestData::class)
class TrendSeriesTest :
    ShouldSpec({
        should("total the values without an aggregated value") {
            series(1.0, 2.0, 3.0).total shouldBe 6.0
            series().total shouldBe 0.0
        }

        should("prefer the aggregated value over the sum") {
            series(1.0, 2.0, 3.0, aggregatedValue = 2.5).total shouldBe 2.5
        }

        should("have no change without a previous period") {
            series(10.0).changePercent shouldBe null
        }

        should("have no change when the previous total is zero") {
            series(10.0, previous = series(0.0, 0.0)).changePercent shouldBe null
            series(10.0, previous = series()).changePercent shouldBe null
        }

        should("give the change against the previous total in percent") {
            series(75.0, 75.0, previous = series(50.0, 50.0)).changePercent shouldBe 50.0
            series(25.0, previous = series(100.0)).changePercent shouldBe -75.0
            series(0.0, previous = series(40.0)).changePercent shouldBe -100.0
        }

        should("compare aggregated values when both periods have one") {
            series(1.0, aggregatedValue = 30.0, previous = series(1.0, aggregatedValue = 20.0)).changePercent shouldBe
                50.0
        }

        should("measure the change against the size of a negative previous total") {
            series(50.0, previous = series(-100.0)).changePercent shouldBe 150.0
        }

        should("compare every fixture series to its previous period") {
            TestComparisonResult.series.forEach { trend ->
                val previous = trend.previous!!.total
                trend.changePercent shouldBe (trend.total - previous) / previous * 100
            }
        }
    })
