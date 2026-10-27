package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.links.DeepLinkDestination
import com.nightcode.snout.shared.domain.model.links.LinkResolution
import com.nightcode.snout.shared.domain.model.telemetry.LinkDestination
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import org.koin.core.annotation.Single

/** An incoming link was handled as [resolution]; only the kind of screen goes, never the link. */
@Single
class TrackLinkOpened(private val repository: TelemetryRepository) {

    operator fun invoke(resolution: LinkResolution) = repository.track(
        TelemetryEvent.LinkOpened(resolution.destination()),
    )
}

private fun LinkResolution.destination(): LinkDestination = when (this) {
    is LinkResolution.Browser -> LinkDestination.Browser

    is LinkResolution.InApp -> when (destination) {
        DeepLinkDestination.Home -> LinkDestination.Home
        is DeepLinkDestination.Section -> LinkDestination.Section
        is DeepLinkDestination.Dashboard -> LinkDestination.Dashboard
        is DeepLinkDestination.Insight -> LinkDestination.Insight
        is DeepLinkDestination.FeatureFlag -> LinkDestination.FeatureFlag
        is DeepLinkDestination.ErrorTrackingIssue -> LinkDestination.ErrorTrackingIssue
        is DeepLinkDestination.Experiment -> LinkDestination.Experiment
    }
}
