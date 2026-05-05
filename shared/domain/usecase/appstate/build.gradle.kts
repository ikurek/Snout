plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.usecase.appstate"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.appstate)
        implementation(projects.shared.data.appstate)
        implementation(projects.shared.domain.data.appstate)
        implementation(projects.shared.domain.data.auth)
        implementation(projects.shared.data.auth)
    }
}
