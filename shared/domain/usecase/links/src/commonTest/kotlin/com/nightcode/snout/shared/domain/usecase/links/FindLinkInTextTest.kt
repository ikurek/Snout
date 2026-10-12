package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.model.links.NoLinkInText
import com.nightcode.snout.shared.domain.usecase.appstate.ParsePostHogLink
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec

class FindLinkInTextTest :
    ShouldSpec({
        val subject = FindLinkInText(ParsePostHogLink())

        should("prefer a PostHog link and drop trailing punctuation") {
            subject("Look at https://example.com and https://eu.posthog.com/project/1/insights/aB1.") shouldBeRight
                "https://eu.posthog.com/project/1/insights/aB1"
        }

        should("fall back to the first web link") {
            subject("Only https://example.com/page, nothing else") shouldBeRight "https://example.com/page"
        }

        should("return null for text without links") {
            subject("no links here") shouldBeLeft NoLinkInText
        }
    })
