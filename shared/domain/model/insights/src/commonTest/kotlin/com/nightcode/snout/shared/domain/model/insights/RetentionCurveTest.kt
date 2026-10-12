package com.nightcode.snout.shared.domain.model.insights

import com.nightcode.snout.shared.common.testdata.TestData
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

private val Days = listOf("Day 0", "Day 1", "Day 2", "Day 3")

@OptIn(TestData::class)
class RetentionCurveTest :
    ShouldSpec({
        should("weight each interval by the size of the cohorts that reached it") {
            TestRetentionResult.curve shouldBe listOf(
                1.0,
                (180.0 + 150.0 + 200.0) / (400.0 + 360.0 + 420.0),
                (120.0 + 100.0) / (400.0 + 360.0),
                96.0 / 400.0,
            )
        }

        should("be empty without cohorts") {
            InsightResult.Retention(Days, emptyList()).curve.shouldBeEmpty()
        }

        should("be empty without intervals") {
            InsightResult.Retention(emptyList(), TestRetentionResult.cohorts).curve.shouldBeEmpty()
        }

        should("end at the last interval any cohort reached") {
            val retention = InsightResult.Retention(Days, listOf(RetentionCohort("2026-10-01", listOf(10.0, 4.0))))
            retention.curve shouldBe listOf(1.0, 0.4)
        }

        should("ignore counts past the last interval") {
            val retention = InsightResult.Retention(
                listOf("Day 0", "Day 1"),
                listOf(RetentionCohort("2026-10-01", listOf(10.0, 5.0, 2.0))),
            )
            retention.curve shouldBe listOf(1.0, 0.5)
        }

        should("skip empty cohorts") {
            val retention = InsightResult.Retention(
                Days,
                listOf(
                    RetentionCohort("2026-10-01", listOf(0.0, 0.0, 0.0, 0.0)),
                    RetentionCohort("2026-10-02", listOf(20.0, 5.0)),
                    RetentionCohort("2026-10-03", emptyList()),
                ),
            )
            retention.curve shouldBe listOf(1.0, 0.25)
        }

        should("be empty when every cohort is empty") {
            InsightResult.Retention(Days, listOf(RetentionCohort("2026-10-01", listOf(0.0, 0.0)))).curve
                .shouldBeEmpty()
        }

        should("give a cohort's rate per interval") {
            val cohort = RetentionCohort("2026-10-01", listOf(200.0, 50.0))
            cohort.size shouldBe 200.0
            cohort.rate(0) shouldBe 1.0
            cohort.rate(1) shouldBe 0.25
            cohort.rate(2) shouldBe null
            RetentionCohort("2026-10-02", listOf(0.0, 0.0)).rate(1) shouldBe 0.0
            RetentionCohort("2026-10-03", emptyList()).size shouldBe 0.0
        }
    })
