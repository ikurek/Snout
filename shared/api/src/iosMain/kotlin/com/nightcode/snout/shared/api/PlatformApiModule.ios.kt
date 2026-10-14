package com.nightcode.snout.shared.api

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@Module
actual class PlatformApiModule {
    /** Xcode's Debug configuration links the debug framework and Release the release one. */
    @OptIn(ExperimentalNativeApi::class)
    @Single
    fun provideAppBuild(): AppBuild = if (Platform.isDebugBinary) AppBuild.Debug else AppBuild.Release
}
