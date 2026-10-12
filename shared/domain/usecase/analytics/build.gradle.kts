plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.analytics"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.analytics)
        implementation(projects.shared.domain.data.analytics)
        implementation(projects.shared.domain.data.appstate)
        api(projects.shared.domain.usecase.appstate)
    }
}
