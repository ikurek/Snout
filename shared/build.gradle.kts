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
        api(projects.shared.domain.model.appstate)
        api(projects.shared.domain.model.auth)
        api(projects.shared.domain.model.common)
        api(projects.shared.domain.model.insights)
        api(projects.shared.domain.model.user)
        api(projects.shared.domain.model.organization)
        api(projects.shared.domain.usecase.analytics)
        api(projects.shared.domain.usecase.auth)
        api(projects.shared.domain.usecase.insights)
        api(projects.shared.domain.usecase.user)
        api(projects.shared.domain.usecase.organization)
        api(projects.shared.domain.usecase.appstate)
    }
}
