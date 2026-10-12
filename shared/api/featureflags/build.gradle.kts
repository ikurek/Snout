plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.api.featureflags"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api)
        implementation(projects.shared.storage.datastore)
    }
}
