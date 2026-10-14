plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.storage.securestore"
    }

    sourceSets.commonMain.dependencies {
        implementation(libs.ksafe)
    }
}
