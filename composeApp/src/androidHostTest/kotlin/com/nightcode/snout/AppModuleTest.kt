package com.nightcode.snout

import io.kotest.core.spec.style.ShouldSpec
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import org.koin.test.verify.verify

/**
 * The Koin compiler plugin checks the shared graph at build time (see SharedGraph in :shared), but can't check
 * this app's graph: composeApp doesn't see the data layer. This checks every constructor in the whole graph,
 * view models included, so a missing use case or data module fails here rather than at runtime.
 */
class AppModuleTest :
    ShouldSpec({
        should("provide every constructor dependency of the app graph") {
            AppModule().module().verify(
                // Constructors of the library types built by provider functions, which never come from Koin.
                extraTypes = listOf(
                    HttpClientEngine::class,
                    HttpClientConfig::class,
                    JsonConfiguration::class,
                    SerializersModule::class,
                ),
            )
        }
    })
