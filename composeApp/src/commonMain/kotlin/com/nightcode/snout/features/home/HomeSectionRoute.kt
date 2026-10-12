package com.nightcode.snout.features.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.sections.label
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.links.resource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeSectionRoute(section: HomeSection, onNavigateBack: () -> Unit, navigation: SectionNavigation) {
    val openInPostHog = rememberPostHogOpener()
    PushedScreenScaffold(
        title = stringResource(section.label),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = { openInPostHog(section.resource) },
    ) { innerPadding ->
        // No bottom bar here, so the list itself takes the navigation bar inset and scrolls behind it.
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom())) {
            HomeSectionContent(
                section = section,
                navigation = navigation,
                contentPadding = innerPadding.bottomOnly(),
            )
        }
    }
}
