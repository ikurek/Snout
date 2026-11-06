package com.nightcode.snout.shared.data.errortracking.mapper

import com.nightcode.snout.shared.api.errortracking.model.ExceptionDto
import com.nightcode.snout.shared.api.errortracking.model.HogQLResultsDto
import com.nightcode.snout.shared.data.testutils.TestJson
import com.nightcode.snout.shared.domain.model.errortracking.ExceptionFrame
import com.nightcode.snout.shared.domain.model.errortracking.SourceContext
import com.nightcode.snout.shared.domain.model.errortracking.SourceLine
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class OccurrenceMapperTest :
    ShouldSpec({
        should("map occurrence rows, naming the person by name, then email, then distinct id") {
            val rows = TestJson.decodeFromString<HogQLResultsDto>(
                """{"results":[
                ["01a1-a","2026-11-06T11:23:58.815000+01:00","Android","12","3.2.4",null,null,"CHZ-LX1",null,
                 "saved_scans","posthog-android","d-1",null,null],
                ["01a1-b","2026-11-05T22:06:16.242000Z","Mac OS X","15.1","",
                 "Chrome","141",null,"https://example.com/cart",null,"posthog-js","d-2","ada@example.com",""],
                [null,"2026-11-05T22:06:16Z"],
                ["01a1-c","not a time"]
                ]}""",
            )

            val occurrences = rows.toOccurrences()

            occurrences.map { it.id } shouldBe listOf("01a1-a", "01a1-b")
            with(occurrences[0]) {
                timestamp shouldBe Instant.parse("2026-11-06T10:23:58.815Z")
                os shouldBe "Android"
                osVersion shouldBe "12"
                appVersion shouldBe "3.2.4"
                deviceModel shouldBe "CHZ-LX1"
                screen shouldBe "saved_scans"
                url.shouldBeNull()
                person shouldBe "d-1"
            }
            with(occurrences[1]) {
                appVersion.shouldBeNull()
                browser shouldBe "Chrome"
                url shouldBe "https://example.com/cart"
                person shouldBe "ada@example.com"
            }
        }

        should("map a resolved exception with the throwing frame first and its source context") {
            val exception = TestJson.decodeFromString<ExceptionDto>(
                """{"type":"TypeError","value":"Cannot read 'price'","mechanism":{"handled":false,"synthetic":false},
                "stacktrace":{"type":"resolved","frames":[
                  {"raw_id":"1","mangled_name":"a","resolved_name":"main","source":"node_modules/react.js",
                   "line":10,"column":2,"in_app":false,"resolved":true,"lang":"javascript"},
                  {"raw_id":"2","mangled_name":"b","resolved_name":"CartSummary","source":"src/Cart.tsx",
                   "line":42,"column":17,"in_app":true,"resolved":true,"lang":"javascript",
                   "context":{"before":[{"number":41,"line":"const first = items[0]"}],
                   "line":{"number":42,"line":"const total = first.price"},
                   "after":[{"number":43,"line":"return total"}]}}
                ]}}""",
            ).toOccurrenceException()

            exception.type shouldBe "TypeError"
            exception.message shouldBe "Cannot read 'price'"
            exception.handled shouldBe false
            exception.frames shouldBe listOf(
                ExceptionFrame(
                    function = "CartSummary",
                    module = null,
                    source = "src/Cart.tsx",
                    line = 42,
                    column = 17,
                    inApp = true,
                    context = SourceContext(
                        lines = listOf(
                            SourceLine(41, "const first = items[0]"),
                            SourceLine(42, "const total = first.price"),
                            SourceLine(43, "return total"),
                        ),
                        errorLine = 42,
                    ),
                ),
                ExceptionFrame("main", null, "node_modules/react.js", 10, 2, inApp = false, context = null),
            )
        }

        should("fall back to the SDK's raw frame keys and drop unknown line numbers") {
            val exception = TestJson.decodeFromString<ExceptionDto>(
                """{"stacktrace":{"type":"raw","frames":[
                  {"function":"onClick","filename":"app.js","lineno":7,"colno":3,"in_app":true},
                  {"mangled_name":"a","module":"zzb26","line":0,"lang":"java","in_app":false,
                   "context":{"before":[],"after":[]}}
                ]}}""",
            ).toOccurrenceException()

            exception.type shouldBe "Error"
            exception.message.shouldBeNull()
            exception.handled.shouldBeNull()
            exception.frames shouldBe listOf(
                ExceptionFrame("a", "zzb26", null, null, null, inApp = false, context = null),
                ExceptionFrame("onClick", null, "app.js", 7, 3, inApp = true, context = null),
            )
        }

        should("map an exception without a stack to no frames") {
            TestJson.decodeFromString<ExceptionDto>("""{"type":"Error"}""").toOccurrenceException()
                .frames.shouldBeEmpty()
        }
    })
