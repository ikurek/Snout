package com.nightcode.snout.shared.data.links

import android.content.Context
import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformLinksDataModule {
    @Single(binds = [ExternalAppLauncher::class])
    fun provideExternalAppLauncher(context: Context): ExternalAppLauncher = AndroidExternalAppLauncher(context)
}
