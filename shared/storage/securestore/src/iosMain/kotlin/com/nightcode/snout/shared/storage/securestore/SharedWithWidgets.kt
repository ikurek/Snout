package com.nightcode.snout.shared.storage.securestore

import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager

/**
 * Where the app keeps what its widget extension reads too: the App Group container, where the preferences file
 * lives, and a Keychain access group, where the app keeps the API key both of them use. Both are named in the
 * targets' entitlements (`iosApp/iosApp/iosApp.entitlements` and `iosApp/SnoutWidgets/SnoutWidgets.entitlements`).
 */
object SharedWithWidgets {
    const val AppGroupId = "group.com.nightcode.snout"

    /** The access group with its team prefix, which only signing knows: each target's Info.plist resolves it. */
    private const val KeychainAccessGroupKey = "SnoutKeychainAccessGroup"

    /** The App Group container's path, or null in a build without the App Group entitlement. */
    val directory: String?
        get() = NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(AppGroupId)?.path

    /** True in the widget extension, which only ever reads what the app shares. */
    val isAppExtension: Boolean
        get() = NSBundle.mainBundle.bundlePath.endsWith(".appex")

    /** Both targets have it: a build without it can't share the key, so it stops here rather than sign in alone. */
    internal val keychainAccessGroup: String
        get() = checkNotNull(
            (NSBundle.mainBundle.objectForInfoDictionaryKey(KeychainAccessGroupKey) as? String)
                ?.takeIf { it.isNotBlank() },
        ) { "$KeychainAccessGroupKey is missing from Info.plist" }
}
