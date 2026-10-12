package com.nightcode.snout.shared.domain.usecase.appstate

/**
 * A request to load the selected project's data again, emitted by the `Observe…Reloads` use cases. Equal values
 * belong to the same project, so a screen may keep what it shows while it reloads; a different value means another
 * project was selected and what is on screen belongs to the previous one.
 */
class ProjectReload internal constructor(private val projectId: Long) {
    override fun equals(other: Any?): Boolean = other is ProjectReload && other.projectId == projectId

    override fun hashCode(): Int = projectId.hashCode()

    override fun toString(): String = "ProjectReload($projectId)"
}
