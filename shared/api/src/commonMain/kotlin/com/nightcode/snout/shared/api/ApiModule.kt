package com.nightcode.snout.shared.api

import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import com.nightcode.snout.shared.storage.datastore.DataStoreModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * The app's single [Json], for PostHog and for the account kept in the secure store; tests use it too. Request DTOs
 * rely on `encodeDefaults` being off: their optional fields default to null and are then left out, as PostHog expects.
 */
val ApiJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

@Module(
    includes = [DataStoreModule::class],
)
class ApiModule {

    @Single
    fun provideJson(): Json = ApiJson

    @Single
    fun providePostHogHttpClient(json: Json, dataStore: AuthDataStore): HttpClient = platformHttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
        // No defaults: engine timeouts stay as they are; slow endpoints opt into longer ones per request.
        install(HttpTimeout)
        install(Auth) {
            bearer {
                // Read the key per request: a cached one would outlive logging out and connecting another key.
                cacheTokens = false
                loadTokens {
                    dataStore.getPersonalApiKey()?.let {
                        BearerTokens(accessToken = it, refreshToken = null)
                    }
                }
                sendWithoutRequest { true }
            }
        }
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                if (exception is ClientRequestException &&
                    exception.response.status == HttpStatusCode.Unauthorized
                ) {
                    // Keep the instance URL so the app reopens straight at the key step, not the intro.
                    dataStore.clearPersonalApiKey()
                }
            }
        }
    }
}
