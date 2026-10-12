package com.nightcode.snout.shared.data.links

import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/** iOS never hands https links to Snout, so the system's default handler is always a browser. */
internal class IosExternalAppLauncher : ExternalAppLauncher {

    override val canOpenLinkSettings = false

    override fun openInBrowser(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    /** There is no such setting on iOS. */
    override fun openLinkSettings() = Unit
}
