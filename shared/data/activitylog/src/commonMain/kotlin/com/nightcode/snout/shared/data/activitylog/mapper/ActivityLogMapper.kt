package com.nightcode.snout.shared.data.activitylog.mapper

import com.nightcode.snout.shared.api.activitylog.model.ActivityChangeDto
import com.nightcode.snout.shared.api.activitylog.model.ActivityLogDto
import com.nightcode.snout.shared.data.common.nullIfBlank
import com.nightcode.snout.shared.domain.model.activitylog.ActivityChange
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private const val MaxValueLength = 120
private const val MaxInlineListSize = 3

internal fun ActivityLogDto.toEntry(): ActivityEntry = ActivityEntry(
    id = id,
    actor = user?.displayName.nullIfBlank(),
    isSystem = isSystem,
    activity = activity,
    scope = scope,
    itemName = detail?.name.nullIfBlank() ?: detail?.shortId,
    changes = detail?.changes.orEmpty().mapNotNull { it.toChange() },
    createdAt = createdAt,
)

private fun ActivityChangeDto.toChange(): ActivityChange? {
    val name = field ?: type ?: return null
    return ActivityChange(field = name, action = action, before = before.summarized(), after = after.summarized())
}

/** Short, human readable rendering of a change value; large structures are summarized. */
private fun JsonElement?.summarized(): String? = when (this) {
    null, JsonNull -> null

    is JsonPrimitive -> contentOrNull?.take(MaxValueLength)

    is JsonArray -> if (size <= MaxInlineListSize && all { it is JsonPrimitive }) {
        joinToString(", ") { it.summarized().orEmpty() }
    } else {
        "$size items"
    }

    is JsonObject -> if (isEmpty()) null else "{…}"
}
