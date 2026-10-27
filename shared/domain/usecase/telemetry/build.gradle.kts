plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.telemetry"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.telemetry)
        api(projects.shared.domain.model.links)
        implementation(projects.shared.domain.data.telemetry)
        implementation(projects.shared.domain.usecase.auth)
    }

    sourceSets.commonTest.dependencies {
        implementation(projects.shared.domain.data.auth)
    }
}
