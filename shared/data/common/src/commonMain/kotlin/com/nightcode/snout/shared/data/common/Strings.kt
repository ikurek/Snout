package com.nightcode.snout.shared.data.common

/** PostHog sends empty or whitespace-only text where nothing was entered; treat it as absent. */
fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
