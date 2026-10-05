package com.nightcode.snout.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.intialproject.InitialProjectNavKey
import com.nightcode.snout.features.login.LoginNavKey
import com.nightcode.snout.features.settings.SettingsNavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val DefaultSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeNavKey::class, HomeNavKey.serializer())
            subclass(LoginNavKey::class, LoginNavKey.serializer())
            subclass(InitialProjectNavKey::class, InitialProjectNavKey.serializer())
            subclass(SettingsNavKey::class, SettingsNavKey.serializer())
        }
    }
}
