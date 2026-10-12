package com.nightcode.snout.shared.data.insights

import com.nightcode.snout.shared.api.insights.DashboardsApi
import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.data.testutils.testJsonCacheStore
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first

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
            testJsonCacheStore(),
        )
        var dashboardResponse = dashboardJson(firstResult = seriesJson(1, 2), secondResult = seriesJson(3))
        postHog.onRequest { request ->
            when (request.url.encodedPath) {
                DashboardPath -> respondJson(dashboardResponse)
                InsightPath -> respondJson(insightJson(InsightId, seriesJson(7, 8)))
                else -> respondError(HttpStatusCode.NotFound)
            }
        }

        suspend fun cachedDashboard() = repository.observeDashboard(ProjectId, DashboardId).first()

        should("keep cached tile results that a refresh returns without one") {
            repository.refreshDashboard(ProjectId, DashboardId).shouldBeRight()
            dashboardResponse = dashboardJson(firstResult = "null", secondResult = seriesJson(4))

            repository.refreshDashboard(ProjectId, DashboardId).shouldBeRight()

            val dashboard = cachedDashboard().shouldNotBeNull()
            dashboard.insightTile(1).result.values() shouldBe listOf(1.0, 2.0)
            dashboard.insightTile(2).result.values() shouldBe listOf(4.0)
            dashboard.tiles[2] shouldBe DashboardTile.TextTile(id = 3, body = "Notes")
        }

        should("put a refreshed tile result into the cached dashboard") {
            dashboardResponse = dashboardJson(firstResult = "null", secondResult = seriesJson(3))
            repository.refreshDashboard(ProjectId, DashboardId).shouldBeRight()

            repository.refreshTile(ProjectId, DashboardId, InsightId).shouldBeRight()

            val dashboard = cachedDashboard().shouldNotBeNull()
            dashboard.insightTile(1).result.values() shouldBe listOf(7.0, 8.0)
            dashboard.insightTile(2).result.values() shouldBe listOf(3.0)
        }

        should("leave the cache empty when refreshing a tile of a dashboard never loaded") {
            repository.refreshTile(ProjectId, DashboardId, InsightId).shouldBeRight()

            cachedDashboard().shouldBeNull()
        }

        should("keep the cached dashboard when a tile refresh fails") {
            repository.refreshDashboard(ProjectId, DashboardId).shouldBeRight()
            val before = cachedDashboard()
            postHog.onRequest { respondError(HttpStatusCode.TooManyRequests) }

            repository.refreshTile(ProjectId, DashboardId, InsightId) shouldBeLeft DataError.RateLimited

            cachedDashboard() shouldBe before
        }
    })
