package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.api.insights.model.InsightDto
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

class InsightMapperTest :
    ShouldSpec({
        val json = Json { ignoreUnknownKeys = true }

        should("derive kind and display from an InsightVizNode trends query") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","name":"","derived_name":"Pageviews",
                "query":{"kind":"InsightVizNode","source":{"kind":"TrendsQuery","trendsFilter":{"display":"BoldNumber"}}},
                "result":[{"label":"${'$'}pageview","aggregated_value":4200,"data":[]}]}""",
            )
            val detail = dto.toDetail()
            detail.summary.name shouldBe "Pageviews"
            detail.summary.kind shouldBe InsightKind.Trends
            detail.summary.display shouldBe InsightDisplay.Number
            detail.result.shouldBeInstanceOf<InsightResult.Series>().series.single().total shouldBe 4200.0
        }

        should("map trend series with breakdown labels and skip previous period") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"InsightVizNode","source":{"kind":"TrendsQuery"}},
                "result":[
                 {"label":"${'$'}pageview","breakdown_value":"Chrome","data":[1,2],"days":["2026-10-01","2026-10-02"]},
                 {"label":"${'$'}pageview","compare_label":"previous","data":[9,9],"days":["2026-09-29","2026-09-30"]}
                ]}""",
            )
            val series = dto.toDetail().result.shouldBeInstanceOf<InsightResult.Series>().series
            series.size shouldBe 1
            series[0].label shouldBe "${'$'}pageview · Chrome"
            series[0].values shouldBe listOf(1.0, 2.0)
            series[0].periods shouldBe listOf("2026-10-01", "2026-10-02")
        }

        should("map funnel steps with conversion relative to first step") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"InsightVizNode","source":{"kind":"FunnelsQuery"}},
                "result":[
                  {"name":"visit","count":100,"order":0},
                  {"name":"buy","custom_name":"Paid","count":25,"order":1,"median_conversion_time":42.5}
                ]}""",
            )
            val detail = dto.toDetail()
            detail.summary.display shouldBe InsightDisplay.Funnel
            val steps = detail.result.shouldBeInstanceOf<InsightResult.Funnel>().steps
            steps[1].name shouldBe "Paid"
            steps[1].conversionRate shouldBe 0.25
            steps[1].medianConversionSeconds shouldBe 42.5
        }

        should("map SQL rows into a table using top-level columns") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"DataTableNode","source":{"kind":"HogQLQuery","query":"select 1"}},
                "columns":["event","count"],"result":[["${'$'}pageview",12],["click",null]]}""",
            )
            val table = dto.toDetail().result.shouldBeInstanceOf<InsightResult.Table>()
            table.columns shouldBe listOf("event", "count")
            table.rows shouldBe listOf(listOf("${'$'}pageview", "12"), listOf("click", ""))
        }

        should("report unsupported kinds and null result when not computed") {
            val retention = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a",
                "query":{"kind":"InsightVizNode","source":{"kind":"RetentionQuery"}},"result":[]}""",
            )
            retention.toDetail().result shouldBe InsightResult.Unsupported
            val pending = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a",
                "query":{"kind":"InsightVizNode","source":{"kind":"TrendsQuery"}},"result":null}""",
            )
            pending.toDetail().result shouldBe null
        }
    })
