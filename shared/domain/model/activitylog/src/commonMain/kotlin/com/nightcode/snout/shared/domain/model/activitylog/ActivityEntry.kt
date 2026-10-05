package com.nightcode.snout.shared.domain.model.activitylog

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ActivityChange(val field: String, val action: String?, val before: String?, val after: String?)

@Serializable
data class ActivityEntry(
    val id: String,
    val actor: String?,
    val isSystem: Boolean,
    /** Verb as PostHog reports it: created, updated, deleted, ... */
    val activity: String,
    /** Resource type as PostHog reports it: FeatureFlag, Insight, Dashboard, ... */
    val scope: String,
    val itemName: String?,
    val changes: List<ActivityChange>,
    val createdAt: Instant,
)

@TestData
val TestActivityEntry = ActivityEntry(
    id = "a1",
    actor = "Jane Doe",
    isSystem = false,
    activity = "updated",
    scope = "FeatureFlag",
    itemName = "new-checkout",
    changes = listOf(ActivityChange(field = "active", action = "changed", before = "false", after = "true")),
    createdAt = Instant.parse("2026-10-05T07:45:00Z"),
)
