package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher

/** Opens [url] in a browser, never back in Snout. */
class OpenInBrowser(private val launcher: ExternalAppLauncher) {
    operator fun invoke(url: String) = launcher.openInBrowser(url)
}
