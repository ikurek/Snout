package com.nightcode.snout.shared.domain.usecase.telemetry

import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.links.DeepLinkDestination
import com.nightcode.snout.shared.domain.model.links.LinkResolution
import com.nightcode.snout.shared.domain.model.telemetry.LinkDestination
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.core.spec.style.ShouldSpec

class TrackLinkOpenedTest :
    ShouldSpec({
        val repository = mock<TelemetryRepository>(autoUnit)

        should("report a link shown in the app only as the kind of screen it led to") {
            TrackLinkOpened(repository)(LinkResolution.InApp(DeepLinkDestination.FeatureFlag(id = 42)))

            verify { repository.track(TelemetryEvent.LinkOpened(LinkDestination.FeatureFlag)) }
        }

        should("report a link handed to the browser without its address") {
            TrackLinkOpened(repository)(LinkResolution.Browser("https://eu.posthog.com/project/1/replay/home"))

            verify { repository.track(TelemetryEvent.LinkOpened(LinkDestination.Browser)) }
        }
    })
