package com.nightcode.snout.shared.domain.model.telemetry

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class InstanceKindTest :
    ShouldSpec({
        should("recognise PostHog Cloud, with or without a trailing slash") {
            InstanceKind.of("https://us.posthog.com") shouldBe InstanceKind.CloudUs
            InstanceKind.of("https://app.posthog.com/") shouldBe InstanceKind.CloudUs
            InstanceKind.of("https://eu.posthog.com/") shouldBe InstanceKind.CloudEu
        }

        should("call every other instance self-hosted") {
            InstanceKind.of("https://posthog.example.com") shouldBe InstanceKind.SelfHosted
            InstanceKind.of("https://example.com/eu.posthog.com") shouldBe InstanceKind.SelfHosted
        }
    })
