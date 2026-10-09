plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.logs"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.logs)
        implementation(projects.shared.data.logs)
        implementation(projects.shared.domain.data.logs)
        implementation(projects.shared.domain.data.appstate)
        implementation(projects.shared.domain.usecase.appstate)
    }
}
