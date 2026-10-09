package com.nightcode.snout.features.home

import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.bottom_nav_analytics
import snout.composeapp.generated.resources.bottom_nav_dashboards
import snout.composeapp.generated.resources.bottom_nav_error_tracking
import snout.composeapp.generated.resources.bottom_nav_feature_flags
import snout.composeapp.generated.resources.bottom_nav_insights
import snout.composeapp.generated.resources.ic_analytics_24dp
import snout.composeapp.generated.resources.ic_bug_report_24dp
import snout.composeapp.generated.resources.ic_dashboard_24dp
import snout.composeapp.generated.resources.ic_flag_24dp
import snout.composeapp.generated.resources.ic_history_24dp
import snout.composeapp.generated.resources.ic_insights_24dp
import snout.composeapp.generated.resources.ic_science_24dp
import snout.composeapp.generated.resources.ic_sticky_note_24dp
import snout.composeapp.generated.resources.more_activity_log_subtitle
import snout.composeapp.generated.resources.more_activity_log_title
import snout.composeapp.generated.resources.more_annotations_subtitle
import snout.composeapp.generated.resources.more_annotations_title
import snout.composeapp.generated.resources.more_experiments_subtitle
import snout.composeapp.generated.resources.more_experiments_title
import snout.composeapp.generated.resources.section_analytics_description
import snout.composeapp.generated.resources.section_dashboards_description
import snout.composeapp.generated.resources.section_error_tracking_description
import snout.composeapp.generated.resources.section_feature_flags_description
import snout.composeapp.generated.resources.section_insights_description

/** Label used in the bottom bar, app bar and settings. */
internal val HomeSection.label: StringResource
    get() = when (this) {
        HomeSection.Analytics -> Res.string.bottom_nav_analytics
        HomeSection.Dashboards -> Res.string.bottom_nav_dashboards
        HomeSection.Insights -> Res.string.bottom_nav_insights
        HomeSection.FeatureFlags -> Res.string.bottom_nav_feature_flags
        HomeSection.ErrorTracking -> Res.string.bottom_nav_error_tracking
        HomeSection.Experiments -> Res.string.more_experiments_title
        HomeSection.Annotations -> Res.string.more_annotations_title
        HomeSection.ActivityLog -> Res.string.more_activity_log_title
    }

/** One-line description shown under the label in More and in navigation settings. */
internal val HomeSection.description: StringResource
    get() = when (this) {
        HomeSection.Analytics -> Res.string.section_analytics_description
        HomeSection.Dashboards -> Res.string.section_dashboards_description
        HomeSection.Insights -> Res.string.section_insights_description
        HomeSection.FeatureFlags -> Res.string.section_feature_flags_description
        HomeSection.ErrorTracking -> Res.string.section_error_tracking_description
        HomeSection.Experiments -> Res.string.more_experiments_subtitle
        HomeSection.Annotations -> Res.string.more_annotations_subtitle
        HomeSection.ActivityLog -> Res.string.more_activity_log_subtitle
    }

internal val HomeSection.icon: DrawableResource
    get() = when (this) {
        HomeSection.Analytics -> Res.drawable.ic_analytics_24dp
        HomeSection.Dashboards -> Res.drawable.ic_dashboard_24dp
        HomeSection.Insights -> Res.drawable.ic_insights_24dp
        HomeSection.FeatureFlags -> Res.drawable.ic_flag_24dp
        HomeSection.ErrorTracking -> Res.drawable.ic_bug_report_24dp
        HomeSection.Experiments -> Res.drawable.ic_science_24dp
        HomeSection.Annotations -> Res.drawable.ic_sticky_note_24dp
        HomeSection.ActivityLog -> Res.drawable.ic_history_24dp
    }

/** Page opened by the "Open in PostHog" action while the section is on screen. */
internal val HomeSection.resource: PostHogResource
    get() = when (this) {
        HomeSection.Analytics -> PostHogResource.WebAnalytics
        HomeSection.Dashboards -> PostHogResource.Dashboards
        HomeSection.Insights -> PostHogResource.Insights
        HomeSection.FeatureFlags -> PostHogResource.FeatureFlags
        HomeSection.ErrorTracking -> PostHogResource.ErrorTracking
        HomeSection.Experiments -> PostHogResource.Experiments
        HomeSection.Annotations -> PostHogResource.Annotations
        HomeSection.ActivityLog -> PostHogResource.ActivityLog
    }
