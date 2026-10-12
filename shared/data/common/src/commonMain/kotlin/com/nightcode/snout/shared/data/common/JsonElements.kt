package com.nightcode.snout.shared.data.common

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

/** The content of a JSON primitive; null for JSON null, objects, arrays and a missing element. */
fun JsonElement?.string(): String? = (this as? JsonPrimitive)?.contentOrNull

/** A JSON primitive read as a number; null when it isn't one or is missing. */
fun JsonElement?.number(): Double? = (this as? JsonPrimitive)?.doubleOrNull

/** The primitive content under [key], see [string]. */
fun JsonObject.stringValue(key: String): String? = get(key).string()
