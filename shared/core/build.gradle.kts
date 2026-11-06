plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.core"
    }

    // All api: the composition roots built on CoreModule (:shared for the app, :shared:widgets for the iOS widget
    // extension) check their graphs at build time, which only sees Koin definitions on their own classpath. They
    // depend on this module as implementation, so the data layer stays off the UI's classpath.
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
        api(projects.shared.domain.model.persons)
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
        api(projects.shared.domain.usecase.persons)
        api(projects.shared.domain.usecase.appstate)
        api(projects.shared.domain.usecase.telemetry)
        api(projects.shared.data.activitylog)
        api(projects.shared.data.analytics)
        api(projects.shared.data.annotations)
        api(projects.shared.data.appstate)
        api(projects.shared.data.auth)
        api(projects.shared.data.common)
        api(projects.shared.data.errortracking)
        api(projects.shared.data.experiments)
        api(projects.shared.data.featureflags)
        api(projects.shared.data.insights)
        api(projects.shared.data.links)
        api(projects.shared.data.logs)
        api(projects.shared.data.organization)
        api(projects.shared.data.persons)
        api(projects.shared.data.telemetry)
        api(projects.shared.data.user)
        api(projects.shared.api.activitylog)
        api(projects.shared.api.analytics)
        api(projects.shared.api.annotations)
        api(projects.shared.api.errortracking)
        api(projects.shared.api.experiments)
        api(projects.shared.api.featureflags)
        api(projects.shared.api.insights)
        api(projects.shared.api.logs)
        api(projects.shared.api.organizations)
        api(projects.shared.api.persons)
        api(projects.shared.api.users)
    }
}
