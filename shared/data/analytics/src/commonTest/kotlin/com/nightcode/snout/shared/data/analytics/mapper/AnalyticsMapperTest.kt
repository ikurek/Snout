package com.nightcode.snout.shared.data.analytics.mapper

import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class AnalyticsMapperTest :
    ShouldSpec({
        val json = Json { ignoreUnknownKeys = true }

        should("map web overview items") {
            val response = json.decodeFromString<QueryResponseDto>(
                """{"results":[
                    {"key":"visitors","value":120.0,"previous":100.0,"changeFromPreviousPct":20.0,"kind":"unit"},
                    {"key":"bounce rate","value":0.4,"previous":0.5,"changeFromPreviousPct":-20.0,
                     "kind":"percentage","isIncreaseBad":true}
                ]}""",
            )
            val metrics = response.toOverviewMetrics(json)
            metrics[0].key shouldBe OverviewMetricKey.Visitors
            metrics[0].changePercent shouldBe 20.0
            metrics[1].kind shouldBe OverviewMetricKind.Percentage
            metrics[1].isIncreaseBad shouldBe true
        }

        should("sum pageview and screen series into one visitors time series") {
            val response = json.decodeFromString<QueryResponseDto>(
                """{"results":[
                  {"label":"${'$'}pageview","data":[1.0,2.0],"days":["2026-10-01","2026-10-02"]},
                  {"label":"${'$'}screen","data":[0.0,5.0],"days":["2026-10-01","2026-10-02"]}
                ]}""",
            )
            response.toTimeSeries(json)?.points?.map { it.label to it.value } shouldBe
                listOf("2026-10-01" to 1.0, "2026-10-02" to 7.0)
        }

        should("map stats table rows using column names and tuple cells, skipping the null bucket") {
            val response = json.decodeFromString<QueryResponseDto>(
                """{"columns":["context.columns.breakdown_value","context.columns.visitors","context.columns.views"],
                "results":[["/home",[12,10],[30,25]],[null,5,9]]}""",
            )
            val table = response.toBreakdownTable(WebBreakdown.Page)
            table.rows[0].label shouldBe "/home"
            table.rows[0].visitors shouldBe 12.0
            table.rows[0].views shouldBe 30.0
            table.rows.size shouldBe 1
        }
    })
