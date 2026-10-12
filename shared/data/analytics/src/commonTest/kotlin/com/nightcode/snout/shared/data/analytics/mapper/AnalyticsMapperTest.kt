package com.nightcode.snout.shared.data.analytics.mapper

import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.data.testutils.TestJson
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import com.nightcode.snout.shared.domain.model.analytics.TrendMetric
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class AnalyticsMapperTest :
    ShouldSpec({
        should("map web overview items") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"results":[
                    {"key":"visitors","value":120.0,"previous":100.0,"changeFromPreviousPct":20.0,"kind":"unit"},
                    {"key":"bounce rate","value":0.4,"previous":0.5,"changeFromPreviousPct":-20.0,
                     "kind":"percentage","isIncreaseBad":true}
                ]}""",
            )
            val metrics = response.toOverviewMetrics(TestJson)
            metrics[0].key shouldBe OverviewMetricKey.Visitors
            metrics[0].changePercent shouldBe 20.0
            metrics[1].kind shouldBe OverviewMetricKind.Percentage
            metrics[1].isIncreaseBad shouldBe true
        }

        should("sum pageview and screen series per metric and keep the previous period apart") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"results":[
                  {"action":{"custom_name":"visitors"},"compare_label":"current",
                   "data":[1.0,2.0],"days":["2026-10-01","2026-10-02"]},
                  {"action":{"custom_name":"visitors"},"compare_label":"current",
                   "data":[0.0,5.0],"days":["2026-10-01","2026-10-02"]},
                  {"action":{"custom_name":"visitors"},"compare_label":"previous",
                   "data":[3.0,3.0],"days":["2026-09-29","2026-09-30"]},
                  {"action":{"custom_name":"views"},"compare_label":"current",
                   "data":[10.0,20.0],"days":["2026-10-01","2026-10-02"]}
                ]}""",
            )
            val trends = response.toMetricTrends(TestJson)
            trends.map { it.metric } shouldBe listOf(TrendMetric.Visitors, TrendMetric.Views)
            trends[0].current.points.map { it.label to it.value } shouldBe
                listOf("2026-10-01" to 1.0, "2026-10-02" to 7.0)
            trends[0].previous?.points?.map { it.value } shouldBe listOf(3.0, 3.0)
            trends[1].previous shouldBe null
        }

        should("take active users as of the last day and compare with the previous period") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"results":[
                  {"action":{"custom_name":"wau"},"compare_label":"current","data":[80.0,100.0]},
                  {"action":{"custom_name":"wau"},"compare_label":"previous","data":[70.0,80.0]},
                  {"action":{"custom_name":"mau"},"compare_label":"current","data":[300.0,310.0]}
                ]}""",
            )
            val metrics = response.toActiveUserMetrics(TestJson)
            metrics.map { it.key } shouldBe
                listOf(OverviewMetricKey.WeeklyActiveUsers, OverviewMetricKey.MonthlyActiveUsers)
            metrics[0].value shouldBe 100.0
            metrics[0].previous shouldBe 80.0
            metrics[0].changePercent!! shouldBe (25.0 plusOrMinus 0.001)
            metrics[1].changePercent shouldBe null
        }

        should("map stats table rows using column names and tuple cells, skipping the null bucket") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"columns":["context.columns.breakdown_value","context.columns.visitors","context.columns.views"],
                "results":[["/home",[12,10],[30,25]],[null,5,9]]}""",
            )
            val table = response.toBreakdownTable(WebBreakdown.Page)
            table.rows[0].label shouldBe "/home"
            table.rows[0].visitors shouldBe 12.0
            table.rows[0].views shouldBe 30.0
            table.rows.size shouldBe 1
        }

        should("map property breakdowns into a sorted top list without sentinel buckets") {
            val response = TestJson.decodeFromString<QueryResponseDto>(
                """{"results":[
                  {"breakdown_value":"2.4.4","aggregated_value":40.0,"action":{"event":"${'$'}screen"}},
                  {"breakdown_value":"2.4.3","aggregated_value":120.0,"action":{"event":"${'$'}screen"}},
                  {"breakdown_value":"2.4.4","aggregated_value":5.0,"action":{"event":"${'$'}pageview"}},
                  {"breakdown_value":"${'$'}${'$'}_posthog_breakdown_null_${'$'}${'$'}","aggregated_value":9.0}
                ]}""",
            )
            val table = response.toPropertyBreakdownTable(TestJson, WebBreakdown.AppVersion)
            table.rows.map { it.label to it.visitors } shouldBe listOf("2.4.3" to 120.0, "2.4.4" to 45.0)
        }
    })
