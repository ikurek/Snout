package com.nightcode.snout.shared.domain.model.insights

import com.nightcode.snout.shared.common.testdata.TestData
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class FunnelBreakdownTest :
    ShouldSpec({
        should("count who entered the first step and converted at the last") {
            val chrome = TestFunnelResult.breakdowns.first()
            chrome.entered shouldBe 700.0
            chrome.converted shouldBe 140.0
            chrome.conversionRate shouldBe 140.0 / 700.0
        }

        should("treat a single step as fully converted") {
            val breakdown = FunnelBreakdown("Chrome", listOf(FunnelStep("Visited pricing", 12.0, 1.0, null)))
            breakdown.conversionRate shouldBe 1.0
        }

        should("be zero without steps") {
            val breakdown = FunnelBreakdown("", emptyList())
            breakdown.entered shouldBe 0.0
            breakdown.converted shouldBe 0.0
            breakdown.conversionRate shouldBe 0.0
        }

        should("be zero when nobody entered") {
            val breakdown = FunnelBreakdown(
                "Safari",
                listOf(FunnelStep("Visited pricing", 0.0, 1.0, null), FunnelStep("Paid", 0.0, 0.0, null)),
            )
            breakdown.conversionRate shouldBe 0.0
        }
    })
