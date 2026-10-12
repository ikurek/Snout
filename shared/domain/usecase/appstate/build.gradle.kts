plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.appstate"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.appstate)
        api(projects.shared.domain.model.analytics)
        implementation(projects.shared.data.appstate)
        implementation(projects.shared.domain.data.appstate)
        implementation(projects.shared.domain.data.auth)
        implementation(projects.shared.data.auth)
    }
}
