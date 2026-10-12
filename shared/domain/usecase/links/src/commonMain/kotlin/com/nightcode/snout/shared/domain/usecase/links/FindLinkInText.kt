package com.nightcode.snout.shared.domain.usecase.links

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.nightcode.snout.shared.domain.model.links.NoLinkInText
import com.nightcode.snout.shared.domain.usecase.appstate.ParsePostHogLink

private val UrlPattern = Regex("""https?://[^\s<>"']+""")

/**
 * The link in shared or pasted text: apps share a title and a link ("Checkout error\nhttps://…"), not a bare
 * URL. The first PostHog page wins; otherwise the first web link, which then opens in the browser.
 */
class FindLinkInText(private val parsePostHogLink: ParsePostHogLink) {
    operator fun invoke(text: String): Either<NoLinkInText, String> {
        val urls = UrlPattern.findAll(text).map { it.value.trimEnd('.', ',', ';', ':', ')', ']', '!', '?') }.toList()
        val url = urls.firstOrNull { parsePostHogLink(it).isRight() } ?: urls.firstOrNull()
        return url?.right() ?: NoLinkInText.left()
    }
}
