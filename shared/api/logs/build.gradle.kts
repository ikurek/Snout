plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.api.logs"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api)
        implementation(projects.shared.storage.datastore)
    }

    sourceSets.commonTest.dependencies {
        implementation(projects.shared.data.testutils)
    }
}
