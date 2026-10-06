package com.nightcode.snout.features.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.designsystem.organisms.ListScreenScaffold
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

/** A section opened from the More list: the same content as a tab, pushed with its own app bar. */
@Serializable
internal data class SectionNavKey(val section: HomeSection) : NavKey

@Composable
internal fun SectionRoute(section: HomeSection, onNavigateBack: () -> Unit, navigation: SectionNavigation) {
    ListScreenScaffold(
        title = stringResource(section.label),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = rememberOpenInPostHog(section.resource),
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HomeSectionContent(section = section, navigation = navigation)
        }
    }
}
