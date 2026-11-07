package com.nightcode.snout.features.widgets

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class HighlightsWidgetSettingsTest :
    ShouldSpec({
        should("move a metric up or down the widget's order") {
            listOf("a", "b", "c").moved(2, 1) shouldBe listOf("a", "c", "b")
            listOf("a", "b", "c").moved(0, 1) shouldBe listOf("b", "a", "c")
        }

        should("leave the order as it is when a move would leave the list") {
            listOf("a", "b").moved(0, -1) shouldBe listOf("a", "b")
            listOf("a", "b").moved(1, 2) shouldBe listOf("a", "b")
        }
    })
