package com.nightcode.snout

import androidx.compose.runtime.Composable
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.intialproject.InitialProjectNavKey
import com.nightcode.snout.features.login.LoginNavKey
import com.nightcode.snout.navigation.MainNavGraph
import com.nightcode.snout.shared.domain.usecase.appstate.DetermineInitialLocation
import com.nightcode.snout.shared.domain.usecase.appstate.InitialLocation
import kotlinx.coroutines.runBlocking
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import org.koin.plugin.module.dsl.module

@Composable
fun App() = KoinApplication(
    configuration = koinConfiguration {
        module<AppModule>()
    },
) {
    val determineInitialLocation = koinInject<DetermineInitialLocation>()
    val initial = when (runBlocking { determineInitialLocation() }) {
        InitialLocation.Login -> LoginNavKey
        InitialLocation.ProjectSelection -> InitialProjectNavKey
        InitialLocation.Home -> HomeNavKey
    }

    SnoutTheme {
        MainNavGraph(initial = initial)
    }
}
