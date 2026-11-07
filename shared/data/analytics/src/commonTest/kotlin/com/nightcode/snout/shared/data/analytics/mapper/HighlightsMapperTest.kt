package com.nightcode.snout.shared.data.analytics.mapper

import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.data.testutils.TestJson
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class HighlightsMapperTest :
    ShouldSpec({
        should("take daily active users from the last whole day, and weekly and monthly from the last") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"results":[
                  {"custom_name":"dau","compare_label":"current","data":[40.0,50.0,12.0]},
                  {"custom_name":"dau","compare_label":"previous","data":[30.0,40.0,10.0]},
                  {"custom_name":"wau","compare_label":"current","data":[200.0,210.0,220.0]},
                  {"custom_name":"mau","compare_label":"current","data":[900.0,950.0,1000.0]}
                ]}""",
            )
            response.toActiveUserHighlights(TestJson) shouldBe mapOf(
                HighlightMetric.DailyActiveUsers to Highlight.Figure(50.0, OverviewMetricKind.Count, 25.0),
                HighlightMetric.WeeklyActiveUsers to Highlight.Figure(220.0, OverviewMetricKind.Count, null),
                HighlightMetric.MonthlyActiveUsers to Highlight.Figure(1000.0, OverviewMetricKind.Count, null),
                // 50 of the 950 monthly users of the same day; no monthly users the week before to compare with.
                HighlightMetric.Stickiness to Highlight.Figure(50.0 / 950.0, OverviewMetricKind.Percentage, null),
            )
        }

        should("give revenue figures in the project's currency, and none before revenue analytics is set up") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"results":[[1200.5,"8800.25",54,12,30]]}""",
            )
            response.toRevenueHighlights(TestJson, currency = "PLN") shouldBe mapOf(
                HighlightMetric.Mrr to Highlight.Money(1200.5, "PLN"),
                HighlightMetric.Revenue to Highlight.Money(8800.25, "PLN"),
                HighlightMetric.Subscribers to Highlight.Figure(12.0, OverviewMetricKind.Count, null),
                HighlightMetric.PayingCustomers to Highlight.Figure(30.0, OverviewMetricKind.Count, null),
            )
            val notSetUp = TestJson.decodeFromString<QueryResponseDto>("""{"results":[[null,null,0,0,0]]}""")
            notSetUp.toRevenueHighlights(TestJson, currency = null) shouldBe mapOf(
                HighlightMetric.Mrr to Highlight.None,
                HighlightMetric.Revenue to Highlight.None,
                HighlightMetric.Subscribers to Highlight.None,
                HighlightMetric.PayingCustomers to Highlight.None,
            )
        }

        should("say there are no subscriptions, not MRR of nothing, when customers paid but none recurs") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"results":[[0.0,"280.95",54,0,6]]}""",
            )
            val highlights = response.toRevenueHighlights(TestJson, currency = "PLN")
            highlights[HighlightMetric.Mrr] shouldBe Highlight.NoSubscriptions
            highlights[HighlightMetric.Subscribers] shouldBe Highlight.NoSubscriptions
            highlights[HighlightMetric.Revenue] shouldBe Highlight.Money(280.95, "PLN")
        }

        should("pick the newest version by its numbers, whichever is used most, skipping events without one") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"results":[["3.9.2",28515],[null,239],["3.10.0",1589],["3.2.4",5199]]}""",
            )
            response.toLatestAppVersion(TestJson) shouldBe Highlight.Version("3.10.0")
            TestJson.decodeFromString<QueryResponseDto>("""{"results":[]}""")
                .toLatestAppVersion(TestJson) shouldBe Highlight.None
        }

        should("count exceptions and the users who ran into them against the period before, more being worse") {
            val response = TestJson.decodeFromString<QueryResponseDto>("""{"results":[[206,229,40,32]]}""")
            val highlights = response.toExceptionHighlights(TestJson)
            val exceptions = highlights.getValue(HighlightMetric.Exceptions) as Highlight.Figure
            exceptions.value shouldBe 206.0
            exceptions.isIncreaseBad shouldBe true
            (exceptions.changePercent!! * 10).toInt() shouldBe -100
            highlights[HighlightMetric.UsersWithErrors] shouldBe
                Highlight.Figure(40.0, OverviewMetricKind.Count, 25.0, isIncreaseBad = true)
        }
    })
