package com.nightcode.snout.shared.data.insights

import com.nightcode.snout.shared.api.insights.DashboardsApi
import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.HttpStatusCode

private const val ProjectId = 1L
private const val DashboardId = 3L
private const val InsightId = 10L
private const val DashboardPath = "/api/projects/$ProjectId/dashboards/$DashboardId/"
private const val InsightPath = "/api/projects/$ProjectId/insights/$InsightId/"

private const val TrendsQuery = """{"kind":"InsightVizNode","source":{"kind":"TrendsQuery"}}"""

private fun insightJson(id: Long, result: String) =
    """{"id":$id,"short_id":"s$id","name":"Insight $id","query":$TrendsQuery,"result":$result}"""

private fun seriesJson(vararg values: Int) = """[{"label":"views","data":[${values.joinToString(",")}]}]"""

/** A dashboard with insight [InsightId] on tile 1, insight 11 on tile 2 and a text tile 3. */
private fun dashboardJson(firstResult: String, secondResult: String) = """{"id":$DashboardId,"name":"Overview","tiles":[
    {"id":1,"insight":${insightJson(InsightId, firstResult)}},
    {"id":2,"insight":${insightJson(11, secondResult)}},
    {"id":3,"text":{"body":"Notes"}}]}"""

private fun Dashboard.insightTile(id: Long) = tiles.single {
    it.id == id
}.shouldBeInstanceOf<DashboardTile.InsightTile>()

private fun InsightResult?.values() = shouldBeInstanceOf<InsightResult.Series>().series.single().values

class DashboardRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = DashboardRepositoryImpl(
            DashboardsApi(postHog.client, postHog.authDataStore),
            InsightsApi(postHog.client, postHog.authDataStore),
        )
        postHog.onRequest { request ->
            when (request.url.encodedPath) {
                DashboardPath -> respondJson(dashboardJson(firstResult = "null", secondResult = seriesJson(3)))
                InsightPath -> respondJson(insightJson(InsightId, seriesJson(7, 8)))
                else -> respondError(HttpStatusCode.NotFound)
            }
        }

        should("map the dashboard's tiles, leaving results PostHog didn't send empty") {
            val dashboard = repository.getDashboard(ProjectId, DashboardId).shouldBeRight()

            dashboard.insightTile(1).result.shouldBeNull()
            dashboard.insightTile(2).result.values() shouldBe listOf(3.0)
            dashboard.tiles[2] shouldBe DashboardTile.TextTile(id = 3, body = "Notes")
        }

        should("compute a tile's result in the context of its dashboard") {
            repository.getTileResult(ProjectId, DashboardId, InsightId).shouldBeRight().values() shouldBe
                listOf(7.0, 8.0)

            postHog.requests.last().url.parameters["from_dashboard"] shouldBe DashboardId.toString()
        }

        should("report a tile that fails to compute") {
            postHog.onRequest { respondError(HttpStatusCode.TooManyRequests) }

            repository.getTileResult(ProjectId, DashboardId, InsightId) shouldBeLeft DataError.RateLimited
        }
    })
