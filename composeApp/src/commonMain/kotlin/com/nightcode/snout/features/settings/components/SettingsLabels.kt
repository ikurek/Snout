package com.nightcode.snout.features.settings.components

import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.settings_theme_dark
import snout.composeapp.generated.resources.settings_theme_light
import snout.composeapp.generated.resources.settings_theme_system

internal val ThemeMode.label: StringResource
    get() = when (this) {
        ThemeMode.System -> Res.string.settings_theme_system
        ThemeMode.Light -> Res.string.settings_theme_light
        ThemeMode.Dark -> Res.string.settings_theme_dark
    }
