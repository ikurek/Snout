package com.nightcode.snout.features.intialproject

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatMonthYear
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.initial_project_screen_subtitle
import snout.composeapp.generated.resources.initial_project_screen_title
import kotlin.time.Instant

@Composable
internal fun InitialProjectScreen(firstName: String, dateJoined: Instant) {
    Scaffold(
        topBar = {
            TopBar(firstName = firstName, dateJoined = dateJoined)
        },
    ) {
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(firstName: String, dateJoined: Instant) {
    MediumFlexibleTopAppBar(
        title = { Text(stringResource(Res.string.initial_project_screen_title, firstName)) },
        subtitle = {
            Text(
                stringResource(
                    Res.string.initial_project_screen_subtitle,
                    dateJoined.formatMonthYear(),
                ),
            )
        },
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
internal fun InitialProjectScreenEmptyPreview() = InitialProjectScreen(
    firstName = "Igor",
    dateJoined = Instant.fromEpochMilliseconds(0),
)
