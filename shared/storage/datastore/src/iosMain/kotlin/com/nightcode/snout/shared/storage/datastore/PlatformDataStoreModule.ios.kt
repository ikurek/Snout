package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.emptyPreferences
import com.nightcode.snout.shared.storage.securestore.SharedWithWidgets
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Module
actual class PlatformDataStoreModule {
    @Single
    fun provideDataStore(): DataStore<Preferences> = if (SharedWithWidgets.isAppExtension) {
        AppPreferencesReader(preferencesPath().toPath())
    } else {
        PreferenceDataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = { preferencesPath().toPath() },
            ),
        )
    }
}

/**
 * The widget extension's view of the app's preferences: the file as it is now, read again on every collection, and
 * never written. A DataStore reads its file once and then serves it from memory, which in a long-lived extension
 * process would keep an instance URL the app has since replaced. The app's DataStore writes through a temporary file
 * it renames, so a read sees either the old file or the new one, never half of one.
 */
private class AppPreferencesReader(private val path: Path) : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow { emit(read()) }

    /** The app is the one writer: the extension leaves its preferences as they are. */
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences = read()

    private suspend fun read(): Preferences = withContext(Dispatchers.IO) {
        if (FileSystem.SYSTEM.exists(path)) {
            FileSystem.SYSTEM.read(path) { PreferencesSerializer.readFrom(this) }
        } else {
            emptyPreferences()
        }
    }
}

/**
 * In the App Group container, where the widget extension reads the instance URL, the selected project and the
 * analytics range; only the app writes it. Versions before widgets kept it in Documents: the app moves it over once.
 * Without the App Group (a build that lacks the entitlement, or a move that failed) it stays in Documents.
 */
@OptIn(ExperimentalForeignApi::class)
private fun preferencesPath(): String {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val documentsPath = requireNotNull<NSURL>(documents).path + "/$PreferencesFileName"
    val sharedPath = SharedWithWidgets.directory?.let { "$it/$PreferencesFileName" } ?: return documentsPath
    val files = NSFileManager.defaultManager
    val needsMove = !SharedWithWidgets.isAppExtension &&
        !files.fileExistsAtPath(sharedPath) &&
        files.fileExistsAtPath(documentsPath)
    return when {
        !needsMove -> sharedPath
        files.moveItemAtPath(documentsPath, toPath = sharedPath, error = null) -> sharedPath
        else -> documentsPath
    }
}
