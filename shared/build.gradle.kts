plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.analytics)
        api(projects.shared.domain.model.experiments)
        api(projects.shared.domain.model.errortracking)
        api(projects.shared.domain.model.annotations)
        api(projects.shared.domain.model.activitylog)
        api(projects.shared.domain.model.appstate)
        api(projects.shared.domain.model.auth)
        api(projects.shared.domain.model.common)
        api(projects.shared.domain.model.featureflags)
        api(projects.shared.domain.model.insights)
        api(projects.shared.domain.model.links)
        api(projects.shared.domain.model.logs)
        api(projects.shared.domain.model.user)
        api(projects.shared.domain.model.organization)
        api(projects.shared.domain.model.telemetry)
        api(projects.shared.domain.usecase.analytics)
        api(projects.shared.domain.usecase.experiments)
        api(projects.shared.domain.usecase.errortracking)
        api(projects.shared.domain.usecase.annotations)
        api(projects.shared.domain.usecase.activitylog)
        api(projects.shared.domain.usecase.auth)
        api(projects.shared.domain.usecase.featureflags)
        api(projects.shared.domain.usecase.insights)
        api(projects.shared.domain.usecase.links)
        api(projects.shared.domain.usecase.logs)
        api(projects.shared.domain.usecase.user)
        api(projects.shared.domain.usecase.organization)
        api(projects.shared.domain.usecase.appstate)
        api(projects.shared.domain.usecase.telemetry)
        // Every data and use case module, through CoreModule; implementation, so the UI never sees the data layer.
        implementation(projects.shared.core)
        // PostHog behind the telemetry interfaces, bound here so the layers that report never see the SDK.
        implementation(projects.shared.telemetry.sdk)
    }
}
