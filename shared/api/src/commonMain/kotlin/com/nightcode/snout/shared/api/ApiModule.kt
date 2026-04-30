package com.nightcode.snout.shared.api

import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import com.nightcode.snout.shared.storage.datastore.DataStoreModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [DataStoreModule::class],
)
class ApiModule {

    @Single
    fun json() = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Single
    fun postHogHttpClient(json: Json, dataStore: AuthDataStore) = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
        install(Auth) {
            bearer {
                cacheTokens = true
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
                    dataStore.clear()
                }
            }
        }
    }
}
