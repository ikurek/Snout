package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher

/** Whether the system has a screen where the user lets Snout open PostHog links (Android, not iOS). */
class CanOpenLinkSettings(private val launcher: ExternalAppLauncher) {
    operator fun invoke(): Boolean = launcher.canOpenLinkSettings
}
