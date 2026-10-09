package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource

class GetPostHogUrl(private val authRepository: AuthRepository, private val appStateRepository: AppStateRepository) {

    suspend operator fun invoke(resource: PostHogResource): String? {
        val instanceUrl = authRepository.getInstanceUrl()
        val projectId = appStateRepository.getSelectedProject()?.projectId
        if (instanceUrl == null || projectId == null) return null
        return "$instanceUrl/project/$projectId/${resource.path()}"
    }

    // One flat branch per resource; splitting it would only hide the URL table.
    @Suppress("CyclomaticComplexMethod")
    private fun PostHogResource.path(): String = when (this) {
        PostHogResource.Project -> ""
        PostHogResource.WebAnalytics -> "web"
        PostHogResource.Dashboards -> "dashboard"
        is PostHogResource.Dashboard -> "dashboard/$id"
        PostHogResource.Insights -> "insights"
        is PostHogResource.Insight -> "insights/$shortId"
        PostHogResource.FeatureFlags -> "feature_flags"
        is PostHogResource.FeatureFlag -> "feature_flags/$id"
        PostHogResource.ErrorTracking -> "error_tracking"
        is PostHogResource.ErrorTrackingIssue -> "error_tracking/$id"
        PostHogResource.Experiments -> "experiments"
        is PostHogResource.Experiment -> "experiments/$id"
        PostHogResource.Annotations -> "data-management/annotations"
        PostHogResource.ActivityLog -> "activity/explore"
    }
}
