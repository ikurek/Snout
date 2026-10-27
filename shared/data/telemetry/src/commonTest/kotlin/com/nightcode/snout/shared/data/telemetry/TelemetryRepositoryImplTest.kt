package com.nightcode.snout.shared.data.telemetry

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.data.testutils.inMemoryTelemetryDataStore
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.telemetry.InstanceKind
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryScreen
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import com.nightcode.snout.shared.telemetry.fake.FakeEventSink
import com.nightcode.snout.shared.telemetry.fake.FakeEventSink.TrackedEvent
import com.nightcode.snout.shared.telemetry.fake.FakeTelemetrySwitch
import com.nightcode.snout.shared.telemetry.fake.FakeTelemetrySwitch.Applied
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

@OptIn(TestData::class)
class TelemetryRepositoryImplTest :
    ShouldSpec({
        lateinit var eventSink: FakeEventSink
        lateinit var telemetrySwitch: FakeTelemetrySwitch
        lateinit var repository: TelemetryRepositoryImpl

        beforeTest {
            eventSink = FakeEventSink()
            telemetrySwitch = FakeTelemetrySwitch()
            repository = TelemetryRepositoryImpl(eventSink, telemetrySwitch, inMemoryTelemetryDataStore())
        }

        context("track") {
            should("send each event under its snake_case name with its properties") {
                repository.track(TelemetryEvent.Onboarded(InstanceKind.CloudEu))
                repository.track(TelemetryEvent.InsightViewed(InsightKind.Funnels))
                repository.track(TelemetryEvent.IssueStatusChanged(IssueStatus.PendingRelease))
                repository.track(TelemetryEvent.AnnotationCreated)

                eventSink.tracked shouldContainExactly listOf(
                    TrackedEvent("onboarded", mapOf("instance" to "cloud_eu")),
                    TrackedEvent("insight_viewed", mapOf("kind" to "funnels")),
                    TrackedEvent("issue_status_changed", mapOf("status" to "pending_release")),
                    TrackedEvent("annotation_created", emptyMap()),
                )
            }

            should("send lists of what was chosen") {
                repository.track(
                    TelemetryEvent.NavigationChanged(
                        tabs = listOf(HomeSection.Analytics, HomeSection.FeatureFlags),
                        landing = HomeSection.FeatureFlags,
                    ),
                )
                repository.track(TelemetryEvent.AnalyticsSettingsChanged(AnalyticsPreferences.Default))

                eventSink.tracked.first() shouldBe TrackedEvent(
                    "navigation_changed",
                    mapOf("tabs" to listOf("analytics", "feature_flags"), "landing" to "feature_flags"),
                )
                eventSink.tracked.last().properties["default_range"] shouldBe "last7_days"
                eventSink.tracked.last().properties["show_trend_chart"] shouldBe true
            }
        }

        should("tell whether this build can report, as the SDK knows") {
            repository.isAvailable() shouldBe true
            TelemetryRepositoryImpl(eventSink, FakeTelemetrySwitch(isAvailable = false), inMemoryTelemetryDataStore())
                .isAvailable() shouldBe false
        }

        should("send screens by their kind") {
            repository.trackScreen(TelemetryScreen.FeatureFlagDetail)

            eventSink.screens shouldContainExactly listOf("feature_flag_detail")
        }

        context("settings") {
            should("read as on while never switched, and apply that at launch") {
                repository.observeSettings().first() shouldBe TelemetrySettings.Default

                repository.applyStoredSettings()

                telemetrySwitch.applied shouldContainExactly listOf(Applied(usage = true, errors = true))
            }

            should("store what was switched and apply it at once") {
                repository.saveSettings(TelemetrySettings(usage = false, errors = true))

                repository.observeSettings().first() shouldBe TelemetrySettings(usage = false, errors = true)
                telemetrySwitch.applied shouldContainExactly listOf(Applied(usage = false, errors = true))
            }
        }
    })
