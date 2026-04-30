plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.appstate"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.domain.data.appstate)
            implementation(projects.shared.storage.datastore)
        }
    }
}
