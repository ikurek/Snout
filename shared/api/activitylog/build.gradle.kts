plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.api.activitylog"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api)
        implementation(projects.shared.storage.datastore)
        implementation(libs.kotlin.serialization.json)
    }
}
