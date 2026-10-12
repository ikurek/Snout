package com.nightcode.snout.shared.data.activitylog.mapper

import com.nightcode.snout.shared.api.activitylog.model.ActivityLogDto
import com.nightcode.snout.shared.data.testutils.TestJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ActivityLogMapperTest :
    ShouldSpec({
        should("map actor, item and readable changes") {
            val dto = TestJson.decodeFromString<ActivityLogDto>(
                """{"id":"a1","user":{"first_name":"Jane","last_name":"Doe"},"activity":"updated","scope":"FeatureFlag",
                "item_id":"7","detail":{"name":"new-checkout","changes":[
                  {"type":"FeatureFlag","action":"changed","field":"active","before":false,"after":true},
                  {"type":"FeatureFlag","action":"changed","field":"filters","before":{"a":1},"after":[1,2,3,4]}]},
                "created_at":"2026-10-05T07:45:00Z"}""",
            )
            val entry = dto.toEntry()
            entry.actor shouldBe "Jane Doe"
            entry.itemName shouldBe "new-checkout"
            entry.changes[0].before shouldBe "false"
            entry.changes[0].after shouldBe "true"
            entry.changes[1].before shouldBe "{…}"
            entry.changes[1].after shouldBe "4 items"
        }
    })
