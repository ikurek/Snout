package com.nightcode.snout.designsystem.charts

import androidx.compose.ui.geometry.Rect
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ChartSelectionTest :
    ShouldSpec({
        val plot = Rect(left = 100f, top = 0f, right = 500f, bottom = 200f)

        should("pick the nearest line point and clamp outside the plot") {
            // Five points at x = 100, 200, 300, 400, 500.
            plot.lineIndexAt(x = 240f, count = 5) shouldBe 1
            plot.lineIndexAt(x = 260f, count = 5) shouldBe 2
            plot.lineIndexAt(x = 20f, count = 5) shouldBe 0
            plot.lineIndexAt(x = 640f, count = 5) shouldBe 4
            plot.lineIndexAt(x = 300f, count = 1) shouldBe 0
        }

        should("pick the bar group under the finger") {
            // Four groups, each 100 wide.
            plot.barIndexAt(x = 101f, count = 4) shouldBe 0
            plot.barIndexAt(x = 299f, count = 4) shouldBe 1
            plot.barIndexAt(x = 499f, count = 4) shouldBe 3
            plot.barIndexAt(x = 50f, count = 4) shouldBe 0
            plot.barIndexAt(x = 900f, count = 4) shouldBe 3
        }
    })
