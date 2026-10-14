package com.nightcode.snout.shared.domain.usecase.links

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class FindLinkInTextTest :
    ShouldSpec({
        val subject = FindLinkInText()

        should("prefer a PostHog link and drop trailing punctuation") {
            subject("Look at https://example.com and https://eu.posthog.com/project/1/insights/aB1.") shouldBe
                "https://eu.posthog.com/project/1/insights/aB1"
        }

        should("fall back to the first web link") {
            subject("Only https://example.com/page, nothing else") shouldBe "https://example.com/page"
        }

        should("return null for text without links") {
            subject("no links here").shouldBeNull()
        }
    })
