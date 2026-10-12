package com.nightcode.snout.features.errortracking.components

import com.nightcode.snout.shared.domain.model.errortracking.IssueSeverity
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.error_tracking_severity_critical
import snout.composeapp.generated.resources.error_tracking_severity_high
import snout.composeapp.generated.resources.error_tracking_severity_low
import snout.composeapp.generated.resources.error_tracking_severity_medium
import snout.composeapp.generated.resources.error_tracking_status_active
import snout.composeapp.generated.resources.error_tracking_status_resolved
import snout.composeapp.generated.resources.error_tracking_status_suppressed

internal val IssueSeverity.label: StringResource
    get() = when (this) {
        IssueSeverity.Low -> Res.string.error_tracking_severity_low
        IssueSeverity.Medium -> Res.string.error_tracking_severity_medium
        IssueSeverity.High -> Res.string.error_tracking_severity_high
        IssueSeverity.Critical -> Res.string.error_tracking_severity_critical
    }

internal val IssueStatusFilter.label: StringResource
    get() = when (this) {
        IssueStatusFilter.Active -> Res.string.error_tracking_status_active
        IssueStatusFilter.Resolved -> Res.string.error_tracking_status_resolved
        IssueStatusFilter.Suppressed -> Res.string.error_tracking_status_suppressed
    }
