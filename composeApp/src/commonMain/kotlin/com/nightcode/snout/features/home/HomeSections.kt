package com.nightcode.snout.features.home

import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_analytics_24dp
import snout.composeapp.generated.resources.ic_bug_report_24dp
import snout.composeapp.generated.resources.ic_dashboard_24dp
import snout.composeapp.generated.resources.ic_flag_24dp
import snout.composeapp.generated.resources.ic_history_24dp
import snout.composeapp.generated.resources.ic_insights_24dp
import snout.composeapp.generated.resources.ic_science_24dp
import snout.composeapp.generated.resources.ic_sticky_note_24dp
import snout.composeapp.generated.resources.ic_subject_24dp
import snout.composeapp.generated.resources.section_activity_log_description
import snout.composeapp.generated.resources.section_activity_log_label
import snout.composeapp.generated.resources.section_analytics_description
import snout.composeapp.generated.resources.section_analytics_label
import snout.composeapp.generated.resources.section_annotations_description
import snout.composeapp.generated.resources.section_annotations_label
import snout.composeapp.generated.resources.section_dashboards_description
import snout.composeapp.generated.resources.section_dashboards_label
import snout.composeapp.generated.resources.section_error_tracking_description
import snout.composeapp.generated.resources.section_error_tracking_label
import snout.composeapp.generated.resources.section_experiments_description
import snout.composeapp.generated.resources.section_experiments_label
import snout.composeapp.generated.resources.section_feature_flags_description
import snout.composeapp.generated.resources.section_feature_flags_label
import snout.composeapp.generated.resources.section_insights_description
import snout.composeapp.generated.resources.section_insights_label
import snout.composeapp.generated.resources.section_logs_description
import snout.composeapp.generated.resources.section_logs_label

/** Label used in the bottom bar, app bar and settings. */
internal val HomeSection.label: StringResource
    get() = when (this) {
        HomeSection.Analytics -> Res.string.section_analytics_label
        HomeSection.Dashboards -> Res.string.section_dashboards_label
        HomeSection.Insights -> Res.string.section_insights_label
        HomeSection.FeatureFlags -> Res.string.section_feature_flags_label
        HomeSection.ErrorTracking -> Res.string.section_error_tracking_label
        HomeSection.Logs -> Res.string.section_logs_label
        HomeSection.Experiments -> Res.string.section_experiments_label
        HomeSection.Annotations -> Res.string.section_annotations_label
        HomeSection.ActivityLog -> Res.string.section_activity_log_label
    }

/** One-line description shown under the label in More and in navigation settings. */
internal val HomeSection.description: StringResource
    get() = when (this) {
        HomeSection.Analytics -> Res.string.section_analytics_description
        HomeSection.Dashboards -> Res.string.section_dashboards_description
        HomeSection.Insights -> Res.string.section_insights_description
        HomeSection.FeatureFlags -> Res.string.section_feature_flags_description
        HomeSection.ErrorTracking -> Res.string.section_error_tracking_description
        HomeSection.Logs -> Res.string.section_logs_description
        HomeSection.Experiments -> Res.string.section_experiments_description
        HomeSection.Annotations -> Res.string.section_annotations_description
        HomeSection.ActivityLog -> Res.string.section_activity_log_description
    }

internal val HomeSection.icon: DrawableResource
    get() = when (this) {
        HomeSection.Analytics -> Res.drawable.ic_analytics_24dp
        HomeSection.Dashboards -> Res.drawable.ic_dashboard_24dp
        HomeSection.Insights -> Res.drawable.ic_insights_24dp
        HomeSection.FeatureFlags -> Res.drawable.ic_flag_24dp
        HomeSection.ErrorTracking -> Res.drawable.ic_bug_report_24dp
        HomeSection.Logs -> Res.drawable.ic_subject_24dp
        HomeSection.Experiments -> Res.drawable.ic_science_24dp
        HomeSection.Annotations -> Res.drawable.ic_sticky_note_24dp
        HomeSection.ActivityLog -> Res.drawable.ic_history_24dp
    }
