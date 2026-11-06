package com.nightcode.snout.shared.domain.model.errortracking

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.time.Instant

data class ExceptionOccurrence(
    val id: String,
    val timestamp: Instant,
    val os: String?,
    val osVersion: String?,
    val appVersion: String?,
    val browser: String?,
    val browserVersion: String?,
    val deviceModel: String?,
    /** `$current_url` on the web. */
    val url: String?,
    /** `$screen_name` in mobile apps. */
    val screen: String?,
    /** SDK that captured it, such as `posthog-android`. */
    val library: String?,
    /** The person's name or email, else their distinct id; null for an anonymous event without one. */
    val person: String?,
)

/** One exception of an occurrence; a chained exception lists its causes as further [OccurrenceException]s. */
data class OccurrenceException(
    val type: String,
    val message: String?,
    /** Whether the app caught it; null when the SDK didn't say. */
    val handled: Boolean?,
    /** The frame that threw first, then its callers. */
    val frames: List<ExceptionFrame>,
)

data class ExceptionFrame(
    /** Function or method, demangled when PostHog could resolve it. */
    val function: String?,
    /** Class or module the function belongs to, where the language has one. */
    val module: String?,
    val source: String?,
    val line: Int?,
    val column: Int?,
    /** Part of the app's own code rather than a library or the platform. */
    val inApp: Boolean,
    /** Source around [line]; only for frames PostHog resolved with their source. */
    val context: SourceContext?,
)

/** Numbered source lines around a frame, with [errorLine] the number of the line that threw. */
data class SourceContext(val lines: List<SourceLine>, val errorLine: Int)

data class SourceLine(val number: Int, val code: String)

@TestData
val TestExceptionOccurrences = listOf(
    ExceptionOccurrence(
        id = "01a1b062-f4f2-766a-8d5b-df6393460e2e",
        timestamp = Instant.parse("2026-10-06T09:41:00Z"),
        os = "Mac OS X",
        osVersion = "15.1",
        appVersion = "2.14.0",
        browser = "Chrome",
        browserVersion = "141",
        deviceModel = null,
        url = "https://shop.example.com/checkout",
        screen = null,
        library = "posthog-js",
        person = "ada@example.com",
    ),
    ExceptionOccurrence(
        id = "01a1ad88-9d9c-7ec7-8adc-26855349c140",
        timestamp = Instant.parse("2026-10-06T08:02:00Z"),
        os = "iOS",
        osVersion = "26.0",
        appVersion = "2.13.2",
        browser = "Mobile Safari",
        browserVersion = "26",
        deviceModel = "iPhone",
        url = "https://shop.example.com/cart",
        screen = null,
        library = "posthog-js",
        person = null,
    ),
)

private fun reactFrame(function: String, line: Int, column: Int) = ExceptionFrame(
    function = function,
    module = null,
    source = "node_modules/react-dom/cjs/react-dom.development.js",
    line = line,
    column = column,
    inApp = false,
    context = null,
)

@TestData
val TestOccurrenceExceptions = listOf(
    OccurrenceException(
        type = "TypeError",
        message = "Cannot read properties of undefined (reading 'price')",
        handled = false,
        frames = listOf(
            ExceptionFrame(
                function = "CartSummary",
                module = null,
                source = "src/checkout/CartSummary.tsx",
                line = 42,
                column = 17,
                inApp = true,
                context = SourceContext(
                    lines = listOf(
                        SourceLine(40, "export function CartSummary({ items }: Props) {"),
                        SourceLine(41, "  const first = items[0]"),
                        SourceLine(42, "  const total = first.price * items.length"),
                        SourceLine(43, "  return <Total value={total} />"),
                        SourceLine(44, "}"),
                    ),
                    errorLine = 42,
                ),
            ),
            reactFrame("renderWithHooks", 16305, 18),
            reactFrame("mountIndeterminateComponent", 20074, 13),
            reactFrame("beginWork", 21587, 16),
            ExceptionFrame(
                function = "CheckoutPage",
                module = null,
                source = "src/checkout/CheckoutPage.tsx",
                line = 88,
                column = 9,
                inApp = true,
                context = null,
            ),
            reactFrame("performUnitOfWork", 26557, 12),
        ),
    ),
)
