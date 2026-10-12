package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import org.koin.core.annotation.Single

/** Whether the system has a screen where the user lets Snout open PostHog links (Android, not iOS). */
@Single
class CanOpenLinkSettings(private val launcher: ExternalAppLauncher) {
    operator fun invoke(): Boolean = launcher.canOpenLinkSettings
}
