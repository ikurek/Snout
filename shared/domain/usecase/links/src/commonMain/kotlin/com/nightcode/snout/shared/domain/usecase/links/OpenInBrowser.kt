package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import org.koin.core.annotation.Single

/** Opens [url] in a browser, never back in Snout. */
@Single
class OpenInBrowser(private val launcher: ExternalAppLauncher) {
    operator fun invoke(url: String) = launcher.openInBrowser(url)
}
