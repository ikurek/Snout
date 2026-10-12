package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.links.DeepLinkDestination
import com.nightcode.snout.shared.domain.model.links.LinkResolution
import com.nightcode.snout.shared.domain.model.links.PostHogLink
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import com.nightcode.snout.shared.domain.model.links.resource
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.insights.FindInsightIdByShortId
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import kotlinx.coroutines.flow.first

/**
 * Turns a PostHog web link into the matching screen. A link to another project the user can access switches
 * the selected project first; anything the app can't show goes to the browser.
 */
@Suppress("LongParameterList") // every step of opening a link is its own use case
class ResolvePostHogLink(
    private val parsePostHogLink: ParsePostHogLink,
    private val getInstanceUrl: GetInstanceUrl,
    private val getSelectedProject: GetSelectedProject,
    private val observeOrganizations: ObserveOrganizations,
    private val refreshOrganizations: RefreshOrganizations,
    private val saveSelectedProject: SaveSelectedProject,
    private val findInsightIdByShortId: FindInsightIdByShortId,
) {
    suspend operator fun invoke(url: String): LinkResolution {
        val link = parsePostHogLink(url).getOrNull()
        val destination = if (link != null && isSignedInTo(link) && selectProject(link)) {
            link.resource.toDestination()
        } else {
            null
        }
        return destination?.let(LinkResolution::InApp) ?: LinkResolution.Browser(url.trim())
    }

    private suspend fun isSignedInTo(link: PostHogLink): Boolean =
        getInstanceUrl()?.let { sameInstance(link.instanceUrl, it) } == true

    /** Makes the link's project the selected one; false when the user's organizations don't have it. */
    private suspend fun selectProject(link: PostHogLink): Boolean {
        if (getSelectedProject()?.projectId == link.projectId) return true
        val organization = findOrganization(link.projectId)
            ?: refreshOrganizations().fold(ifLeft = { null }, ifRight = { findOrganization(link.projectId) })
        organization?.let { saveSelectedProject(it.id, link.projectId) }
        return organization != null
    }

    private suspend fun findOrganization(projectId: Long): Organization? =
        observeOrganizations().first().firstOrNull { organization -> organization.projects.any { it.id == projectId } }

    /** Null when the resource can't be shown after all (an insight short id the API doesn't know). */
    private suspend fun PostHogResource.toDestination(): DeepLinkDestination? = when (this) {
        PostHogResource.Project -> DeepLinkDestination.Home

        is PostHogResource.Dashboard -> DeepLinkDestination.Dashboard(id)

        is PostHogResource.FeatureFlag -> DeepLinkDestination.FeatureFlag(id)

        is PostHogResource.ErrorTrackingIssue -> DeepLinkDestination.ErrorTrackingIssue(id)

        is PostHogResource.Experiment -> DeepLinkDestination.Experiment(id)

        is PostHogResource.Insight -> findInsightIdByShortId(shortId).getOrNull()?.let(DeepLinkDestination::Insight)

        PostHogResource.WebAnalytics,
        PostHogResource.Dashboards,
        PostHogResource.Insights,
        PostHogResource.FeatureFlags,
        PostHogResource.ErrorTracking,
        PostHogResource.Logs,
        PostHogResource.Experiments,
        PostHogResource.Annotations,
        PostHogResource.ActivityLog,
        -> HomeSection.entries.firstOrNull { it.resource == this }?.let(DeepLinkDestination::Section)
    }
}

/** `app.posthog.com` is the old name of the US cloud and still serves the same projects. */
private fun sameInstance(linkInstanceUrl: String, instanceUrl: String): Boolean =
    linkInstanceUrl.canonicalHost() == instanceUrl.canonicalHost()

private fun String.canonicalHost(): String {
    val host = substringAfter("://").substringBefore('/').lowercase()
    return if (host == "app.posthog.com") "us.posthog.com" else host
}
