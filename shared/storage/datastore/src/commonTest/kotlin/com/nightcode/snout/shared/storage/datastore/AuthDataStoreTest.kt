package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.storage.securestore.InMemorySecureStore
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

private const val Key = "phx_key"
private const val Instance = "https://eu.posthog.com"
private val InstanceUrlKey = stringPreferencesKey("instance_url")

@OptIn(TestData::class)
class AuthDataStoreTest :
    ShouldSpec({
        should("read nothing when signed out") {
            AuthDataStore(
                InMemoryDataStore(emptyPreferences()),
                InMemorySecureStore(),
            ).getPersonalApiKey() shouldBe null
        }

        should("save the key only to the secure store") {
            val preferences = InMemoryDataStore(emptyPreferences())
            val secureStore = InMemorySecureStore()
            val subject = AuthDataStore(preferences, secureStore)

            subject.savePersonalApiKey(Key)

            subject.getPersonalApiKey() shouldBe Key
            subject.observePersonalApiKey().first() shouldBe Key
            secureStore.snapshot shouldBe mapOf(PersonalApiKeySecureKey to Key)
            preferences.current.asMap().shouldBeEmpty()
        }

        should("forget only the key after it is rejected, keeping the instance") {
            val preferences = InMemoryDataStore(preferencesOf(InstanceUrlKey to Instance))
            val secureStore = InMemorySecureStore(mapOf(PersonalApiKeySecureKey to Key))
            val subject = AuthDataStore(preferences, secureStore)

            subject.clearPersonalApiKey()

            subject.observePersonalApiKey().first() shouldBe null
            secureStore.snapshot.shouldBeEmpty()
            subject.getInstanceUrl() shouldBe Instance
        }

        should("forget the key and the instance on clear") {
            val preferences = InMemoryDataStore(preferencesOf(InstanceUrlKey to Instance))
            val secureStore = InMemorySecureStore(mapOf(PersonalApiKeySecureKey to Key))
            val subject = AuthDataStore(preferences, secureStore)

            subject.clear()

            subject.getPersonalApiKey() shouldBe null
            subject.getInstanceUrl() shouldBe null
            secureStore.snapshot.shouldBeEmpty()
        }
    })

private class InMemoryDataStore(initial: Preferences) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    val current: Preferences get() = state.value

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(state.value).also { state.value = it }
}
