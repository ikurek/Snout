package com.nightcode.snout.shared.data.featureflags.mapper

import com.nightcode.snout.shared.api.featureflags.model.FeatureFlagDto
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlagStatus
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class FeatureFlagMapperTest :
    ShouldSpec({
        val json = Json { ignoreUnknownKeys = true }

        should("map filters, variants and status") {
            val dto = json.decodeFromString<FeatureFlagDto>(
                """{"id":7,"key":"new-checkout","name":"Redesign","active":true,"status":"STALE",
                "filters":{"groups":[{"properties":[{"key":"email","value":["a@x.com","b@x.com"],"operator":"exact","type":"person"}],
                "rollout_percentage":null,"variant":"test"}],
                "multivariate":{"variants":[{"key":"control","rollout_percentage":50},{"key":"test","name":"New","rollout_percentage":50}]}},
                "created_by":{"first_name":"Jane","last_name":"Doe","email":"jane@x.com"},"tags":["checkout"]}""",
            )
            val flag = dto.toFeatureFlag()
            flag.status shouldBe FeatureFlagStatus.Stale
            flag.conditions.single().rolloutPercentage shouldBe 100
            flag.conditions.single().properties.single().value shouldBe "a@x.com, b@x.com"
            flag.conditions.single().variant shouldBe "test"
            flag.variants.map { it.key } shouldBe listOf("control", "test")
            flag.createdBy shouldBe "Jane Doe"
            flag.isReleasedToEveryone shouldBe false
        }

        should("derive status from active when missing and detect full rollout") {
            val dto = json.decodeFromString<FeatureFlagDto>(
                """{"id":1,"key":"k","active":false,
                "filters":{"groups":[{"properties":[],"rollout_percentage":100}]}}""",
            )
            val flag = dto.toFeatureFlag()
            flag.status shouldBe FeatureFlagStatus.Inactive
            flag.isReleasedToEveryone shouldBe true
        }
    })
