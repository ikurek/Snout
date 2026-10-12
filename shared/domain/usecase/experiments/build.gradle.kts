plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.experiments"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.experiments)
        implementation(projects.shared.domain.data.experiments)
        implementation(projects.shared.domain.data.appstate)
        api(projects.shared.domain.usecase.appstate)
    }
}
