package com.nightcode.snout.features.settings.libraries

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import snout.composeapp.generated.resources.Res

/**
 * The open-source libraries the app ships with and their licenses, as AboutLibraries collected them at build time
 * (see the `aboutLibraries` block in composeApp's build script). Behind an interface so view model tests can hand
 * in their own JSON.
 */
internal fun interface LibraryDefinitions {
    suspend fun load(): Libs
}

/** Reads `files/aboutlibraries.json`, which the build generates into the Android and iOS compose resources. */
@Single(binds = [LibraryDefinitions::class])
internal class BundledLibraryDefinitions : LibraryDefinitions {
    override suspend fun load(): Libs {
        val json = Res.readBytes(DefinitionsPath).decodeToString()
        return withContext(Dispatchers.Default) { Libs.Builder().withJson(json).build() }
    }

    private companion object {
        const val DefinitionsPath = "files/aboutlibraries.json"
    }
}

/**
 * The libraries sorted by name. A missing or unreadable file is an [DataError.UnexpectedError], and so is an empty
 * list: the app always ships libraries, and the Android parser logs a malformed file and returns nothing.
 */
internal suspend fun LibraryDefinitions.loadLibraries(): Either<DataError, List<Library>> =
    Either.catch { load().libraries }
        .mapLeft { DataError.UnexpectedError }
        .flatMap { libraries -> if (libraries.isEmpty()) DataError.UnexpectedError.left() else libraries.right() }
