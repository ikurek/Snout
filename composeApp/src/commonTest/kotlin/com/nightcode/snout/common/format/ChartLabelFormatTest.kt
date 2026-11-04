package com.nightcode.snout.common.format

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ChartLabelFormatTest :
    ShouldSpec({
        should("show a day as its date") {
            "2024-10-05".formatChartLabel() shouldBe "5 Oct"
        }

        should("show a timestamp as its time, with a space or a T") {
            "2024-10-05 14:00:00".formatChartLabel() shouldBe "14:00"
            "2024-10-05T09:30:00.000Z".formatChartLabel() shouldBe "09:30"
            "2024-10-05T09:30".formatChartLabel() shouldBe "09:30"
        }

        should("show anything else as received") {
            "Week 3".formatChartLabel() shouldBe "Week 3"
            "".formatChartLabel() shouldBe ""
        }
    })
