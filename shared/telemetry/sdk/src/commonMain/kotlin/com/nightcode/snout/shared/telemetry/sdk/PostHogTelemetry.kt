package com.nightcode.snout.shared.telemetry.sdk

import com.nightcode.snout.shared.telemetry.ErrorTracker
import com.nightcode.snout.shared.telemetry.EventSink
import com.nightcode.snout.shared.telemetry.TelemetrySwitch
import com.posthog.kmp.ErrorTrackingConfig
import com.posthog.kmp.PersonProfiles
import com.posthog.kmp.PostHog
import com.posthog.kmp.PostHogBeforeSend
import com.posthog.kmp.PostHogConfig
import com.posthog.kmp.PostHogContext
import org.koin.core.annotation.Single
import kotlin.concurrent.Volatile

/** The property [ErrorTracker] reports carry: what the app was doing, worded by the reporting code itself. */
private const val ContextProperty = "context"

/**
 * PostHog behind [EventSink], [ErrorTracker] and [TelemetrySwitch]. It starts PostHog on the first [apply], at launch,
 * with what the person chose; a build that may not report (see [canReport]) never starts it, and everything else is
 * then a no-op. Anonymous: no person profiles and no identify, so events can't be tied to a person or their PostHog
 * account. No automatic screen or deep link events: [screen] sends the app's own, which never name what a screen shows.
 */
@Single(binds = [EventSink::class, ErrorTracker::class, TelemetrySwitch::class])
internal class PostHogTelemetry(platform: TelemetryPlatform) :
    EventSink,
    ErrorTracker,
    TelemetrySwitch {

    private val context: PostHogContext = platform.context
    private val canReport = canReport(platform.isDebugBuild)

    override val isAvailable: Boolean get() = canReport

    @Volatile
    private var started = false

    @Volatile
    private var usage = false

    @Volatile
    private var errors = false

    override fun apply(usage: Boolean, errors: Boolean) {
        if (!canReport) return
        this.usage = usage
        this.errors = errors
        when {
            !started -> start(optOut = !usage && !errors)
            usage || errors -> PostHog.optIn()
            else -> PostHog.optOut()
        }
    }

    override fun track(name: String, properties: Map<String, Any>) {
        if (started && usage) PostHog.capture(name, properties)
    }

    override fun screen(name: String) {
        if (started && usage) PostHog.screen(name)
    }

    override fun trackNonFatal(throwable: Throwable, message: String) {
        if (started && errors) PostHog.captureException(throwable, mapOf(ContextProperty to message))
    }

    private fun start(optOut: Boolean) {
        val config = PostHogConfig(
            projectToken = PostHogSettings.projectToken,
            host = PostHogSettings.host,
            captureScreenViews = false,
            captureDeepLinks = false,
            sendFeatureFlagEvent = false,
            preloadFeatureFlags = false,
            optOut = optOut,
            personProfiles = PersonProfiles.NEVER,
            // The SDK's own events (opens, closes, crashes) pass here too, so this is where the switches hold.
            beforeSend = listOf(
                PostHogBeforeSend { event ->
                    filteredProperties(event.event, event.properties, usage, errors)
                        ?.let { event.copy(properties = it) }
                },
            ),
            errorTracking = ErrorTrackingConfig(autoCapture = true, inAppIncludes = listOf("com.nightcode.snout")),
            capturePushNotificationSubscriptions = false,
            capturePushNotificationOpened = false,
        )
        PostHog.setup(config, context)
        started = true
    }
}

/** Debug builds never report, nor builds without a PostHog project token (see `build.gradle.kts`). */
internal fun canReport(isDebugBuild: Boolean): Boolean = !isDebugBuild && PostHogSettings.projectToken.isNotEmpty()
