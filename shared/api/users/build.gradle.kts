plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.api.users"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.api)
        implementation(projects.shared.storage.datastore)
    }
}
