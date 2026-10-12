plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.links"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.domain.data.links)
        }
    }
}
