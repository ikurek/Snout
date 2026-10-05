package com.nightcode.snout.shared.data.experiments.mapper

import com.nightcode.snout.shared.api.experiments.model.ExperimentDto
import com.nightcode.snout.shared.domain.model.experiments.ExperimentStatus
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class ExperimentMapperTest :
    ShouldSpec({
        val json = Json { ignoreUnknownKeys = true }

        should("map status, variants and metric names") {
            val dto = json.decodeFromString<ExperimentDto>(
                """{"id":3,"name":"Checkout","status":"running","start_date":"2026-09-20T08:00:00Z",
                "feature_flag_key":"new-checkout",
                "feature_flag":{"id":7,"key":"new-checkout","filters":{"multivariate":{"variants":[
                  {"key":"control","rollout_percentage":50},{"key":"test","name":"New","rollout_percentage":50}]}}},
                "metrics":[{"kind":"ExperimentMetric","metric_type":"funnel","name":"Purchase"},
                  {"kind":"ExperimentMetric","metric_type":"mean"}]}""",
            )
            val experiment = dto.toExperiment()
            experiment.status shouldBe ExperimentStatus.Running
            experiment.featureFlagId shouldBe 7L
            experiment.variants.map { it.key } shouldBe listOf("control", "test")
            experiment.metricNames shouldBe listOf("Purchase", "Mean metric")
        }

        should("derive status from dates when missing") {
            json.decodeFromString<ExperimentDto>("""{"id":1,"name":"a"}""").toExperiment().status shouldBe
                ExperimentStatus.Draft
            json.decodeFromString<ExperimentDto>(
                """{"id":1,"name":"a","start_date":"2026-01-01T00:00:00Z","end_date":"2026-02-01T00:00:00Z"}""",
            ).toExperiment().status shouldBe ExperimentStatus.Stopped
        }
    })
