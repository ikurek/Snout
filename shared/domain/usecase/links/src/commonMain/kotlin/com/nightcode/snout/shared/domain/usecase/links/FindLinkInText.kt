package com.nightcode.snout.shared.domain.usecase.links

import org.koin.core.annotation.Single

private val UrlPattern = Regex("""https?://[^\s<>"']+""")

/**
 * The link in shared or pasted text: apps share a title and a link ("Checkout error\nhttps://…"), not a bare
 * URL. The first PostHog page wins; otherwise the first web link, which then opens in the browser. Null when the
 * text has no web link.
 */
@Single
class FindLinkInText(private val parsePostHogLink: ParsePostHogLink) {
    operator fun invoke(text: String): String? {
        val urls = UrlPattern.findAll(text).map { it.value.trimEnd('.', ',', ';', ':', ')', ']', '!', '?') }.toList()
        return urls.firstOrNull { parsePostHogLink(it).isRight() } ?: urls.firstOrNull()
    }
}
