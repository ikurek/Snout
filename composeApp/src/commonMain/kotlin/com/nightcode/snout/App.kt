package com.nightcode.snout

import androidx.compose.runtime.Composable
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.login.LoginNavKey
import com.nightcode.snout.navigation.MainNavGraph
import com.nightcode.snout.shared.domain.usecase.auth.IsPersonalApiKeySaved
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
    val isPersonalApiKeySavedUseCase = koinInject<IsPersonalApiKeySaved>()
    val isPersonalApiKeySaved = runBlocking { isPersonalApiKeySavedUseCase() }

    SnoutTheme {
        MainNavGraph(initial = if (isPersonalApiKeySaved) HomeNavKey else LoginNavKey)
    }
}
