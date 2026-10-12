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
        implementation(projects.shared.data.activitylog)
        implementation(projects.shared.data.analytics)
        implementation(projects.shared.data.annotations)
        implementation(projects.shared.data.appstate)
        implementation(projects.shared.data.auth)
        implementation(projects.shared.data.common)
        implementation(projects.shared.data.errortracking)
        implementation(projects.shared.data.experiments)
        implementation(projects.shared.data.featureflags)
        implementation(projects.shared.data.insights)
        implementation(projects.shared.data.links)
        implementation(projects.shared.data.logs)
        implementation(projects.shared.data.organization)
        implementation(projects.shared.data.user)
    }
}
