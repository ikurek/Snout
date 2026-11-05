package com.nightcode.snout.shared.widgets

import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import org.koin.core.annotation.Single
import kotlin.coroutines.cancellation.CancellationException

/**
 * Brings the widget extension up to date with the app before each load. WidgetKit keeps the extension's process for
 * several timelines, and meanwhile the app may sign out or in: the key the extension loaded before must not outlive
 * that. (Its preferences are read from the app's file on every load already.)
 */
@Single
class WidgetSession(private val authDataStore: AuthDataStore) {

    /** Never throws: a key that can't be read now leaves the load to show that it can't connect. */
    @Suppress("TooGenericExceptionCaught") // whatever the failure, the load that follows reports it
    suspend fun refresh() {
        try {
            authDataStore.reloadPersonalApiKey()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // The load that follows runs with the key it had, and shows its failure if that one is gone.
        }
    }
}
