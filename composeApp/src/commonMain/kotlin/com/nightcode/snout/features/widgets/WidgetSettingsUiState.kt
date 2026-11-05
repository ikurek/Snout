package com.nightcode.snout.features.widgets

import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.widgets.ProjectChoice

internal data class WidgetSettingsUiState(
    /** Whether the app has a key, so there are projects to pick; null until that's known. */
    val isConnected: Boolean? = null,
    /** The projects to pick from; null until they load. */
    val projects: List<ProjectChoice>? = null,
    /** Why the projects couldn't load; [projects] stays null. */
    val projectsError: DataError? = null,
    /** The widget's project; null until one is picked, as for a new widget. */
    val projectId: Long? = null,
)
