package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.model.links.PostHogLink

private const val HttpsScheme = "https://"

/**
 * Reads a PostHog web link (`https://{host}/project/{id}/{page}`) into a [PostHogLink]. Query, fragment and a
 * trailing slash are ignored. Null for anything else, including project pages Snout has no screen for.
 */
fun parsePostHogLink(url: String): PostHogLink? {
    val address = url.trim().takeIf { it.startsWith(HttpsScheme, ignoreCase = true) }
        ?.substring(HttpsScheme.length)?.substringBefore('#')?.substringBefore('?')
        ?: return null
    val host = address.substringBefore('/').lowercase().takeIf { it.isNotEmpty() && it.none(Char::isWhitespace) }
    val segments = address.substringAfter('/', missingDelimiterValue = "").split('/').filter { it.isNotEmpty() }
    val projectId = segments.takeIf { it.firstOrNull() == "project" }?.getOrNull(1)?.toLongOrNull()
    val resource = parsePath(segments.drop(2))
    return if (host != null && projectId != null && resource != null) {
        PostHogLink(instanceUrl = HttpsScheme + host, projectId = projectId, resource = resource)
    } else {
        null
    }
}
