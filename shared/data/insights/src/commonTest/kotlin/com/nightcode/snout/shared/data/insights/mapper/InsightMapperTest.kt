package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.api.insights.model.InsightDto
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import com.nightcode.snout.shared.domain.model.insights.ValueUnit
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

        should("use readable labels for stickiness periods") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"InsightVizNode","source":{"kind":"StickinessQuery"}},
                "result":[{"label":"${'$'}screen","data":[12,1],"days":[1,2],"labels":["1 day","2 days"]}]}""",
            )
            dto.toDetail().result.shouldBeInstanceOf<InsightResult.Series>().series.single().periods shouldBe
                listOf("1 day", "2 days")
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

        should("sum funnel step counts across breakdown values") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"InsightVizNode","source":{"kind":"FunnelsQuery"}},
                "result":[
                  [{"name":"visit","count":60,"breakdown_value":["Chrome"]},
                   {"name":"buy","count":15,"median_conversion_time":10,"breakdown_value":["Chrome"]}],
                  [{"name":"visit","count":40,"breakdown_value":["Safari"]},
                   {"name":"buy","count":5,"median_conversion_time":90,"breakdown_value":["Safari"]}]
                ]}""",
            )
            val steps = dto.toDetail().result.shouldBeInstanceOf<InsightResult.Funnel>().steps
            steps.map { it.name } shouldBe listOf("visit", "buy")
            steps.map { it.count } shouldBe listOf(100.0, 20.0)
            steps[1].conversionRate shouldBe 0.2
            steps[1].medianConversionSeconds shouldBe null
        }

        should("map retention cohorts and weight the curve by cohort size") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"InsightVizNode","source":{"kind":"RetentionQuery"}},
                "result":[
                  {"date":"2026-10-01T00:00:00Z","label":"Day 0",
                   "values":[{"count":100,"label":"Day 0"},{"count":50,"label":"Day 1"},{"count":20,"label":"Day 2"}]},
                  {"date":"2026-10-02T00:00:00Z","label":"Day 1",
                   "values":[{"count":300,"label":"Day 0"},{"count":30,"label":"Day 1"}]}
                ]}""",
            )
            val detail = dto.toDetail()
            detail.summary.display shouldBe InsightDisplay.Line
            val retention = detail.result.shouldBeInstanceOf<InsightResult.Retention>()
            retention.intervals shouldBe listOf("Day 0", "Day 1", "Day 2")
            retention.cohorts.map { it.date } shouldBe listOf("2026-10-01T00:00:00Z", "2026-10-02T00:00:00Z")
            retention.cohorts[1].counts shouldBe listOf(300.0, 30.0)
            retention.cohorts[1].rate(1) shouldBe 0.1
            retention.cohorts[1].rate(2) shouldBe null
            retention.curve shouldBe listOf(1.0, 0.2, 0.2)
        }

        should("add retention breakdown values into one cohort per date") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"InsightVizNode","source":{"kind":"RetentionQuery"}},
                "result":[
                  {"date":"2026-10-01T00:00:00Z","label":"Day 0","breakdown_value":"Chrome",
                   "values":[{"count":10,"label":"Day 0"},{"count":4,"label":"Day 1"}]},
                  {"date":"2026-10-01T00:00:00Z","label":"Day 0","breakdown_value":"Safari",
                   "values":[{"count":30,"label":"Day 0"},{"count":6,"label":"Day 1"}]}
                ]}""",
            )
            val cohorts = dto.toDetail().result.shouldBeInstanceOf<InsightResult.Retention>().cohorts
            cohorts.single().counts shouldBe listOf(40.0, 10.0)
        }

        should("carry the trends aggregation axis format") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"InsightVizNode","source":{"kind":"TrendsQuery",
                "trendsFilter":{"display":"BoldNumber","aggregationAxisFormat":"duration",
                "aggregationAxisPrefix":"~","aggregationAxisPostfix":"","decimalPlaces":1}}},
                "result":[{"label":"Average Session Duration","aggregated_value":46.0,"data":[]}]}""",
            )
            dto.toDetail().result.shouldBeInstanceOf<InsightResult.Series>().format shouldBe
                ValueFormat(unit = ValueUnit.Duration, prefix = "~", postfix = null, decimalPlaces = 1)
        }

        should("map percentage formats and default to numeric") {
            fun format(filter: String) = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"InsightVizNode","source":{"kind":"TrendsQuery",
                "trendsFilter":$filter}},"result":[]}""",
            ).toDetail().result.shouldBeInstanceOf<InsightResult.Series>().format.unit
            format("""{"aggregationAxisFormat":"percentage_scaled"}""") shouldBe ValueUnit.PercentageScaled
            format("""{"aggregationAxisFormat":"duration_ms"}""") shouldBe ValueUnit.DurationMs
            format("{}") shouldBe ValueUnit.Numeric
        }

        should("show a 1x1 SQL BoldNumber visualization as a number") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"DataVisualizationNode","display":"BoldNumber",
                "source":{"kind":"HogQLQuery","query":"select count() from events"},
                "chartSettings":{"yAxis":[{"column":"count()","settings":{"formatting":{"suffix":" events"}}}]}},
                "columns":["count()"],"result":[[1234]]}""",
            )
            val detail = dto.toDetail()
            detail.summary.display shouldBe InsightDisplay.Number
            val series = detail.result.shouldBeInstanceOf<InsightResult.Series>()
            series.series.single().total shouldBe 1234.0
            series.format.postfix shouldBe " events"
        }

        should("chart a SQL line graph from its chart settings columns") {
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"DataVisualizationNode","display":"ActionsLineGraph",
                "source":{"kind":"HogQLQuery","query":"select day, users, views from t"},
                "chartSettings":{"xAxis":{"column":"day"},"yAxis":[{"column":"users"},{"column":"views"}]}},
                "columns":["day","users","views"],
                "result":[["2026-10-01",3,30],["2026-10-02",5,null]]}""",
            )
            val detail = dto.toDetail()
            detail.summary.display shouldBe InsightDisplay.Line
            val series = detail.result.shouldBeInstanceOf<InsightResult.Series>().series
            series.map { it.label } shouldBe listOf("users", "views")
            series[0].periods shouldBe listOf("2026-10-01", "2026-10-02")
            series[0].values shouldBe listOf(3.0, 5.0)
            series[1].values shouldBe listOf(30.0, 0.0)
        }

        should("fall back to a table when a SQL visualization can't be drawn") {
            val noSettings = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"DataVisualizationNode","display":"ActionsLineGraph",
                "source":{"kind":"HogQLQuery","query":"select day, users from t"}},
                "columns":["day","users"],"result":[["2026-10-01",3]]}""",
            )
            noSettings.toDetail().result.shouldBeInstanceOf<InsightResult.Table>()
            val wideNumber = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"DataVisualizationNode","display":"BoldNumber",
                "source":{"kind":"HogQLQuery","query":"select a, b from t"}},
                "columns":["a","b"],"result":[[1,2]]}""",
            )
            wideNumber.toDetail().result.shouldBeInstanceOf<InsightResult.Table>()
        }

        should("keep the SQL row count when trimming table rows") {
            val rows = (1..60).joinToString(",") { "[$it]" }
            val dto = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a","query":{"kind":"HogQLQuery","query":"select n"},
                "columns":["n"],"result":[$rows]}""",
            )
            val table = dto.toDetail().result.shouldBeInstanceOf<InsightResult.Table>()
            table.rows.size shouldBe 50
            table.totalRows shouldBe 60
        }

        should("report unsupported kinds and null result when not computed") {
            val paths = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a",
                "query":{"kind":"InsightVizNode","source":{"kind":"PathsQuery"}},"result":[]}""",
            )
            paths.toDetail().result shouldBe InsightResult.Unsupported
            val pending = json.decodeFromString<InsightDto>(
                """{"id":1,"short_id":"a",
                "query":{"kind":"InsightVizNode","source":{"kind":"TrendsQuery"}},"result":null}""",
            )
            pending.toDetail().result shouldBe null
        }
    })
