package com.nightcode.snout.shared.data.links

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher

internal class AndroidExternalAppLauncher(private val context: Context) : ExternalAppLauncher {

    override val canOpenLinkSettings = true

    /**
     * Once the user lets Snout open posthog.com links, a plain VIEW intent for "Open in PostHog" (or a link Snout
     * can't show) would come straight back to the app, so the intent names a browser.
     */
    override fun openInBrowser(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (uri.scheme == "https" || uri.scheme == "http") browserPackage()?.let(intent::setPackage)
        start(intent)
    }

    override fun openLinkSettings() {
        val packageUri = Uri.fromParts("package", context.packageName, null)
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
        } else {
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
        start(Intent(action, packageUri))
    }

    /** Nothing can open it; there's nothing better to do then. */
    private fun start(intent: Intent) = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
    }

    /** The default browser, else any browser; a host-less link only matches apps that open every web page. */
    private fun browserPackage(): String? {
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://")).addCategory(Intent.CATEGORY_BROWSABLE)
        val packageManager = context.packageManager
        // With no default browser set, the system's chooser ("android") answers instead.
        val default = packageManager.resolveActivity(probe, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
            ?.takeIf { it != "android" && it != context.packageName }
        return default ?: packageManager.queryIntentActivities(probe, 0)
            .map { it.activityInfo.packageName }
            .firstOrNull { it != context.packageName }
    }
}
