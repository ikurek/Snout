package com.nightcode.snout.common.platform

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

@Composable
internal actual fun appVersionName(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0"
