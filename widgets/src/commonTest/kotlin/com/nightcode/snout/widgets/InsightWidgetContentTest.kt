package com.nightcode.snout.widgets

import com.nightcode.snout.shared.common.format.ChangeTone
import com.nightcode.snout.shared.common.format.TrendChange
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.TestComparisonResult
import com.nightcode.snout.shared.domain.model.insights.TestFunnelResult
import com.nightcode.snout.shared.domain.model.insights.TestInsightDetail
import com.nightcode.snout.shared.domain.model.insights.TestRetentionResult
import com.nightcode.snout.shared.domain.model.insights.TestSeriesResult
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import com.nightcode.snout.shared.domain.model.insights.ValueUnit
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

private const val Link = "https://us.posthog.com/project/1/insights/abc123"

@OptIn(TestData::class)
class InsightWidgetContentTest :
    ShouldSpec({
        val summary = TestInsightDetail.summary

        should("show a line's last point under the first series' line, naming PostHog's events as PostHog does") {
            TestInsightDetail.toWidgetContent(Link) shouldBe InsightWidgetContent(
                name = "Daily active users",
                body = InsightWidgetBody.Headline(
                    value = "280",
                    label = "Pageview",
                    change = null,
                    series = listOf(120.0, 180.0, 90.0, 240.0, 200.0, 310.0, 280.0),
                ),
                link = Link,
            )
        }

        should("show a number's total in the insight's format, compared to the previous period") {
            val number = TrendSeries(
                label = "revenue",
                values = listOf(1.0, 2.0),
                periods = listOf("2026-10-01", "2026-10-02"),
                aggregatedValue = 0.25,
                previous = TrendSeries("revenue", listOf(1.0), listOf("2026-09-30"), aggregatedValue = 0.2),
            )
            val detail = TestInsightDetail.copy(
                summary = summary.copy(display = InsightDisplay.Number),
                result = InsightResult.Series(listOf(number), ValueFormat(unit = ValueUnit.PercentageScaled)),
            )
            detail.toWidgetContent(Link).body shouldBe InsightWidgetBody.Headline(
                value = "25%",
                label = "revenue",
                change = TrendChange("↑ 25%", ChangeTone.Good),
                series = emptyList(),
            )
        }

        should("rank breakdown bars by total, named by their breakdown value") {
            val detail = TestInsightDetail.copy(
                summary = summary.copy(display = InsightDisplay.BarValue),
                result = TestComparisonResult,
            )
            val ranking = detail.toWidgetContent(Link).body as InsightWidgetBody.Ranking
            ranking.rows.map { it.label } shouldBe listOf("Chrome", "Safari", "Firefox", "Edge", "Samsung Internet")
        }

        should("rank series of different events by their event") {
            val detail = TestInsightDetail.copy(summary = summary.copy(display = InsightDisplay.Pie))
            (detail.toWidgetContent(Link).body as InsightWidgetBody.Ranking).rows shouldBe listOf(
                RankedValue("Pageview", "1.4k"),
                RankedValue("sign up", "306"),
            )
        }

        should("show how many made it from a funnel's first step through its last") {
            TestInsightDetail.copy(result = TestFunnelResult).toWidgetContent(Link).body shouldBe
                InsightWidgetBody.Headline(
                    value = "17.5%",
                    label = "Visited pricing → Paid",
                    change = null,
                    series = emptyList(),
                )
        }

        should("show a funnel's conversion even when its first and last steps read the same") {
            val step = TestFunnelResult.steps.first()
            val repeated = TestFunnelResult.copy(steps = listOf(step, step), breakdowns = emptyList())
            val body = TestInsightDetail.copy(result = repeated).toWidgetContent(Link).body
            (body as InsightWidgetBody.Headline).value shouldBe "100%"
        }

        should("name every PostHog event in a label as PostHog does") {
            "\$pageview, \$screen".eventLabel() shouldBe "Pageview, Screen"
            "\$pageview · \$direct".eventLabel() shouldBe "Pageview · Direct"
            "sign up".eventLabel() shouldBe "sign up"
        }

        should("offer to open results a widget can't summarise") {
            listOf(TestRetentionResult, InsightResult.Unsupported, null).forEach { result ->
                TestInsightDetail.copy(result = result).toWidgetContent(Link).body shouldBe
                    InsightWidgetBody.Unsupported
            }
            val noSeries = TestInsightDetail.copy(result = TestSeriesResult.copy(series = emptyList()))
            noSeries.toWidgetContent(Link).body shouldBe InsightWidgetBody.Unsupported
        }
    })
