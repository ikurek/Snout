package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.storage.securestore.InMemoryPersonalApiKeyStore
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
                InMemoryPersonalApiKeyStore(),
            ).getPersonalApiKey() shouldBe null
        }

        should("save the key only to its own store") {
            val preferences = InMemoryDataStore(emptyPreferences())
            val keyStore = InMemoryPersonalApiKeyStore()
            val subject = AuthDataStore(preferences, keyStore)

            subject.savePersonalApiKey(Key)

            subject.getPersonalApiKey() shouldBe Key
            subject.observePersonalApiKey().first() shouldBe Key
            keyStore.key shouldBe Key
            preferences.current.asMap().shouldBeEmpty()
        }

        should("pick up a key another process changed in the shared store, once asked to reload it") {
            val keyStore = InMemoryPersonalApiKeyStore(Key)
            val subject = AuthDataStore(InMemoryDataStore(preferencesOf(InstanceUrlKey to Instance)), keyStore)
            subject.getPersonalApiKey() shouldBe Key

            // The app signs out while the widget extension still holds the key it loaded.
            keyStore.clear()
            subject.getPersonalApiKey() shouldBe Key
            subject.reloadPersonalApiKey()
            subject.getPersonalApiKey() shouldBe null

            keyStore.set("phx_other")
            subject.reloadPersonalApiKey()
            subject.observePersonalApiKey().first() shouldBe "phx_other"
        }

        should("drop a key left without its instance, as a reinstalled iOS app finds it") {
            val keyStore = InMemoryPersonalApiKeyStore(Key)
            val subject = AuthDataStore(InMemoryDataStore(emptyPreferences()), keyStore)

            subject.getPersonalApiKey() shouldBe null
            keyStore.key shouldBe null
        }

        should("forget only the key after it is rejected, keeping the instance") {
            val preferences = InMemoryDataStore(preferencesOf(InstanceUrlKey to Instance))
            val keyStore = InMemoryPersonalApiKeyStore(Key)
            val subject = AuthDataStore(preferences, keyStore)

            subject.clearPersonalApiKey()

            subject.observePersonalApiKey().first() shouldBe null
            keyStore.key shouldBe null
            subject.getInstanceUrl() shouldBe Instance
        }

        should("forget the key and the instance on clear") {
            val preferences = InMemoryDataStore(preferencesOf(InstanceUrlKey to Instance))
            val keyStore = InMemoryPersonalApiKeyStore(Key)
            val subject = AuthDataStore(preferences, keyStore)

            subject.clear()

            subject.getPersonalApiKey() shouldBe null
            subject.getInstanceUrl() shouldBe null
            keyStore.key shouldBe null
        }
    })

private class InMemoryDataStore(initial: Preferences) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    val current: Preferences get() = state.value

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(state.value).also { state.value = it }
}
