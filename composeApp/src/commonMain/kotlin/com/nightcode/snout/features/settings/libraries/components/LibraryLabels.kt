package com.nightcode.snout.features.settings.libraries.components

import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License

/** Who makes the library: its first named developer, else its organisation, as its POM lists them. */
internal val Library.author: String?
    get() = developers.firstNotNullOfOrNull { developer -> developer.name?.takeIf { it.isNotBlank() } }
        ?: organization?.name?.takeIf { it.isNotBlank() }

/** Where to read more about the library: its website, else its source repository. */
internal val Library.homepage: String?
    get() = website?.takeIf { it.isNotBlank() } ?: scm?.url?.takeIf { it.startsWith("http") }

/** The SPDX id ("Apache-2.0") where the license has one, its full name otherwise. */
internal val License.shortName: String
    get() = spdxId?.takeIf { it.isNotBlank() } ?: name
