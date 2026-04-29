plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.storage.datastore"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.datastore)
            api(libs.datastore.preferences)
        }
    }
}
