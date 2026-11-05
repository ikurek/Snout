package com.nightcode.snout.widgets

import arrow.core.Either
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

/** A project a widget can show, named with its organization, since names repeat across organizations. */
data class ProjectChoice(val id: Long, val name: String, val organization: String)

/**
 * The projects a widget's settings offer. A new widget starts on none: the person picks its project, which needn't be
 * the one the app shows.
 */
@Single
class WidgetProjects(
    private val isSignedIn: IsSignedIn,
    private val refreshOrganizations: RefreshOrganizations,
    private val observeOrganizations: ObserveOrganizations,
) {
    /**
     * Every project the key can read, fetched from PostHog first: the iOS widget extension keeps its list in memory
     * only, so it has none when it starts. When that fails the stored list stands in, and the failure shows only when
     * there is none.
     */
    suspend fun projects(): WidgetState<List<ProjectChoice>> = isSignedIn.loadWhenSignedIn {
        val refreshed = refreshOrganizations()
        val projects = observeOrganizations().first().flatMap { organization ->
            organization.projects.map { ProjectChoice(it.id, it.name, organization.name) }
        }
        when {
            projects.isNotEmpty() -> WidgetState.Loaded(projects)
            refreshed is Either.Left -> WidgetState.Failed(refreshed.value)
            else -> WidgetState.Loaded(emptyList())
        }
    }
}
