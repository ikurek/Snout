package com.nightcode.snout.shared.storage.securestore

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessGroup
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val Service = "com.nightcode.snout.shared"

/**
 * Strings as generic-password items in the Keychain access group the app shares with its widget extension (see
 * [KeychainPersonalApiKeyStore]). The Keychain encrypts them; they stay on this device and are readable after the
 * first unlock, so a widget can refresh while the phone is locked. Every call goes to the Keychain: nothing is cached.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class SharedKeychain(private val accessGroup: String) {

    fun get(key: String): String? = memScoped {
        val query = query(key)
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)
        when (status) {
            errSecSuccess -> (CFBridgingRelease(result.value) as? NSData)
                ?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString() }

            errSecItemNotFound -> null

            else -> throw SecureStoreException("Could not read $key from the shared Keychain ($status)")
        }
    }

    fun set(key: String, value: String) {
        remove(key)
        val query = query(key)
        query.put(
            kSecValueData,
            requireNotNull(NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding)),
        )
        CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
        val status = SecItemAdd(query, null)
        CFRelease(query)
        if (status != errSecSuccess) throw SecureStoreException("Could not store $key in the shared Keychain ($status)")
    }

    fun remove(key: String) {
        val query = query(key)
        val status = SecItemDelete(query)
        CFRelease(query)
        if (status != errSecSuccess && status != errSecItemNotFound) {
            throw SecureStoreException("Could not delete $key from the shared Keychain ($status)")
        }
    }

    /** The item for [key] in the shared access group; the caller releases it. */
    private fun query(key: String): CFMutableDictionaryRef? {
        val query = CFDictionaryCreateMutable(
            null,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        )
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        query.put(kSecAttrService, NSString.create(string = Service))
        query.put(kSecAttrAccount, NSString.create(string = key))
        query.put(kSecAttrAccessGroup, NSString.create(string = accessGroup))
        return query
    }

    /** Adds an Objective-C [value]; the dictionary retains it, so the bridged reference is released at once. */
    private fun CFMutableDictionaryRef?.put(key: CFStringRef?, value: Any) {
        val bridged: CFTypeRef? = CFBridgingRetain(value)
        CFDictionaryAddValue(this, key, bridged)
        CFRelease(bridged)
    }
}
