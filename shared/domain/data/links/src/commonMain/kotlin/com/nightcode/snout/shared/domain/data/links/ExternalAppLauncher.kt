package com.nightcode.snout.shared.domain.data.links

/** Hands things over to the system: web pages to the browser, link handling to the system settings. */
interface ExternalAppLauncher {
    /** Android only: posthog.com can't vouch for Snout, so the user lets it open those links in settings. */
    val canOpenLinkSettings: Boolean

    /** Always a browser, never Snout, even once Snout may open posthog.com links itself. */
    fun openInBrowser(url: String)

    fun openLinkSettings()
}
