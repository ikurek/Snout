package com.nightcode.snout.shared.telemetry.sdk

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class TelemetryFilterTest :
    ShouldSpec({
        val usageEvent = mapOf<String, Any?>("section" to "dashboards")
        val errorEvent = mapOf<String, Any?>(
            "\$exception_list" to listOf(
                mapOf("type" to "SerializationException", "value" to "Unexpected JSON: {\"name\":\"Q3 revenue\"}"),
            ),
            "\$exception_message" to "Unexpected JSON: {\"name\":\"Q3 revenue\"}",
            "context" to "GET /api/projects/:id/insights/:id/",
        )

        should("send usage events only while usage is on") {
            filteredProperties("section_opened", usageEvent, usage = true, errors = false) shouldBe usageEvent
            filteredProperties("section_opened", usageEvent, usage = false, errors = true).shouldBeNull()
        }

        should("treat the SDK's own events, such as app opens, as usage") {
            filteredProperties("Application Opened", emptyMap(), usage = false, errors = true).shouldBeNull()
        }

        should("send error reports only while errors are on") {
            filteredProperties(ExceptionEvent, errorEvent, usage = true, errors = false).shouldBeNull()
        }

        should("send error reports without their messages, keeping the type and context") {
            filteredProperties(ExceptionEvent, errorEvent, usage = false, errors = true) shouldBe mapOf(
                "\$exception_list" to listOf(mapOf("type" to "SerializationException")),
                "context" to "GET /api/projects/:id/insights/:id/",
            )
        }
    })
