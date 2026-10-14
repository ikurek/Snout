package com.nightcode.snout.shared.api

import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformApiModule {
    /** Debug variants are debuggable and release ones aren't; `:shared` has no `BuildConfig` of the app to read. */
    @Single
    fun provideAppBuild(context: Context): AppBuild =
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) AppBuild.Debug else AppBuild.Release
}
