package com.nightcode.snout.shared.data.logs.mapper

import com.nightcode.snout.shared.api.logs.model.LogServicesResponseDto
import com.nightcode.snout.shared.api.logs.model.LogVolumeBucketDto
import com.nightcode.snout.shared.api.logs.model.LogsQueryResponseDto
import com.nightcode.snout.shared.domain.model.logs.LogLevelCounts
import com.nightcode.snout.shared.domain.model.logs.LogSeverity
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class LogsMapperTest :
    ShouldSpec({
        val json = Json { ignoreUnknownKeys = true }

        should("attach each service's volume buckets in time order") {
            val services = json.decodeFromString<LogServicesResponseDto>(
                """{"services":[
                {"service_name":"api","log_count":300,"error_count":6,"error_rate":0.02,"volume_share_pct":90.0,
                "severity_breakdown":{"debug":10,"info":250,"warn":34,"error":6}},
                {"service_name":"cron","log_count":30,"error_count":0,"error_rate":0.0}],
                "sparkline":[{"time":"2026-10-06T11:00:00Z","service_name":"api","count":200},
                {"time":"2026-10-06T10:00:00Z","service_name":"api","count":100}],
                "total_services":2}""",
            ).toServices()
            services.map { it.name } shouldBe listOf("api", "cron")
            services[0].errorRate shouldBe 0.02
            services[0].volume shouldBe listOf(100L, 200L)
            services[1].volume shouldBe emptyList()
            services[0].levels shouldBe LogLevelCounts(debug = 10, info = 250, warn = 34, error = 6)
            services[1].levels.shouldBeNull()
        }

        should("map entries, blank padded trace IDs and keep the cursor only when there is more") {
            val page = json.decodeFromString<LogsQueryResponseDto>(
                """{"results":[
                {"uuid":"a","timestamp":"2026-10-06T11:58:42.120Z","body":"boom","severity_text":"ERROR",
                "trace_id":"00000000000000000000000000000000","attributes":{"order":"1"},"resource_attributes":{}},
                {"uuid":"b","timestamp":"not a time","body":"lost"},
                {"uuid":"c","timestamp":"2026-10-06T11:58:41Z","severity_text":"warning","trace_id":"4bf9",
                "span_id":"00f067aa0ba902b7","instrumentation_scope":"com.app.Payments","event_name":""}],
                "hasMore":false,"nextCursor":"abc"}""",
            ).toPage()
            page.entries.map { it.id } shouldBe listOf("a", "c")
            page.entries[0].severity shouldBe LogSeverity.Error
            page.entries[0].timestamp shouldBe Instant.parse("2026-10-06T11:58:42.120Z")
            page.entries[0].traceId.shouldBeNull()
            page.entries[0].attributes shouldBe mapOf("order" to "1")
            page.entries[1].severity shouldBe LogSeverity.Warn
            page.entries[1].traceId shouldBe "4bf9"
            page.entries[1].spanId shouldBe "00f067aa0ba902b7"
            page.entries[1].logger shouldBe "com.app.Payments"
            page.entries[1].eventName.shouldBeNull()
            page.entries[0].spanId.shouldBeNull()
            page.nextCursor.shouldBeNull()
        }

        should("send severity filters as PostHog's lowercase levels") {
            LogSeverityFilter.All.apiLevels shouldBe emptyList()
            LogSeverityFilter.Errors.apiLevels shouldBe listOf("error", "fatal")
        }

        should("fold volume buckets into four levels with a value for every time slot") {
            val volume = listOf(
                LogVolumeBucketDto("2026-10-06T11:00:00Z", "info", 40),
                LogVolumeBucketDto("2026-10-06T10:00:00Z", "info", 30),
                LogVolumeBucketDto("2026-10-06T11:00:00Z", "fatal", 1),
                LogVolumeBucketDto("2026-10-06T11:00:00Z", "error", 2),
                LogVolumeBucketDto("2026-10-06T10:00:00Z", "trace", 5),
                LogVolumeBucketDto("2026-10-06T10:00:00Z", "mystery", 9),
                LogVolumeBucketDto("not a time", "info", 7),
            ).toVolume()
            volume.buckets shouldBe listOf(Instant.parse("2026-10-06T10:00:00Z"), Instant.parse("2026-10-06T11:00:00Z"))
            volume.counts shouldBe mapOf(
                LogSeverity.Error to listOf(0L, 3L),
                LogSeverity.Warn to listOf(0L, 0L),
                LogSeverity.Info to listOf(30L, 40L),
                LogSeverity.Debug to listOf(5L, 0L),
            )
        }
    })
