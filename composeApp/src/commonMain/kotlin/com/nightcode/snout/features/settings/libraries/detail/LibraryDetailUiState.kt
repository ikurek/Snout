package com.nightcode.snout.features.settings.libraries.detail

import com.mikepenz.aboutlibraries.entity.Library
import com.nightcode.snout.shared.domain.model.common.DataError

internal data class LibraryDetailUiState(
    /** The library; null until read. */
    val library: Library? = null,
    val error: DataError? = null,
)
