plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.storage.datastore"
    }

    sourceSets.commonMain.dependencies {
        implementation(libs.datastore)
        implementation(libs.datastore.preferences)
    }
}
