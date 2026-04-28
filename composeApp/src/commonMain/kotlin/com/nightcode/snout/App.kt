package com.nightcode.snout

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.nightcode.snout.navigation.MainNavGraph
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.plugin.module.dsl.module

@Composable
fun App() = KoinApplication(configuration = koinConfiguration {
    module<AppModule>()
}) {
    MaterialTheme {
        MainNavGraph()
    }
}
