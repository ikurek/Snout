package com.nightcode.snout.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed

/** Saves the back stack: every [SnoutNavKey] is registered through its sealed serializer, none by hand. */
@OptIn(ExperimentalSerializationApi::class)
internal val DefaultSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed(SnoutNavKey.serializer())
        }
    }
}
