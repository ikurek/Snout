plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.storage.datastore"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.storage.securestore)
        implementation(libs.datastore)
        implementation(libs.datastore.preferences)
    }
}
