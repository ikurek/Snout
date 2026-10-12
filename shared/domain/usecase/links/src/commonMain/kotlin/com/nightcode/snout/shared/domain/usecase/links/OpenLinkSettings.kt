package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher

/** Opens the system screen where the user lets Snout open PostHog links. */
class OpenLinkSettings(private val launcher: ExternalAppLauncher) {
    operator fun invoke() = launcher.openLinkSettings()
}
