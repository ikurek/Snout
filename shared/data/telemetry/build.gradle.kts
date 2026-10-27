plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.telemetry"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.domain.data.telemetry)
        implementation(projects.shared.storage.datastore)
        implementation(projects.shared.telemetry)
    }

    sourceSets.commonTest.dependencies {
        implementation(projects.shared.data.testutils)
    }
}
