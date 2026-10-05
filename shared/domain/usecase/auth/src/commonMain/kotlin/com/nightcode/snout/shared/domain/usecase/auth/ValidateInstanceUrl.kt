package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.nightcode.snout.shared.domain.model.auth.InstanceUrlValidationError

private const val HttpsScheme = "https://"

/**
 * Validates a PostHog instance URL typed by the user and returns its normalized form:
 * trimmed, `https://` prepended when no scheme was given, trailing slashes removed.
 */
class ValidateInstanceUrl {
    operator fun invoke(instanceUrl: String): Either<InstanceUrlValidationError, String> = either {
        val trimmed = instanceUrl.trim()
        ensure(trimmed.isNotEmpty()) { InstanceUrlValidationError.UrlIsEmpty }
        val withScheme = if ("://" in trimmed) trimmed else HttpsScheme + trimmed
        ensure(withScheme.startsWith(HttpsScheme)) { InstanceUrlValidationError.UrlIsInvalid }
        val host = withScheme.removePrefix(HttpsScheme).trimEnd('/')
        ensure(host.isNotEmpty() && host.none { it.isWhitespace() }) { InstanceUrlValidationError.UrlIsInvalid }
        HttpsScheme + host
    }
}
