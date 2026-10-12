package com.nightcode.snout.shared.domain.usecase.appstate

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.nightcode.snout.shared.domain.model.appstate.PostHogLink
import com.nightcode.snout.shared.domain.model.appstate.PostHogLinkError

private const val HttpsScheme = "https://"

/**
 * Reads a PostHog web link (`https://{host}/project/{id}/{page}`) into a [PostHogLink]. Query, fragment and a
 * trailing slash are ignored.
 */
class ParsePostHogLink {
    operator fun invoke(url: String): Either<PostHogLinkError, PostHogLink> = either {
        val trimmed = url.trim()
        ensure(trimmed.startsWith(HttpsScheme, ignoreCase = true)) { PostHogLinkError.NotAProjectLink }
        val address = trimmed.substring(HttpsScheme.length).substringBefore('#').substringBefore('?')
        val host = address.substringBefore('/').lowercase().takeIf { it.isNotEmpty() && it.none(Char::isWhitespace) }
        val segments = address.substringAfter('/', missingDelimiterValue = "").split('/').filter { it.isNotEmpty() }
        val projectId = segments.takeIf { it.firstOrNull() == "project" }?.getOrNull(1)?.toLongOrNull()
        ensure(host != null && projectId != null) { PostHogLinkError.NotAProjectLink }
        val resource = ensureNotNull(parsePath(segments.drop(2))) { PostHogLinkError.UnsupportedPage }
        PostHogLink(instanceUrl = HttpsScheme + host, projectId = projectId, resource = resource)
    }
}
