plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.usecase.auth"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.data.auth)
            implementation(projects.shared.domain.data.auth)
        }
    }
}
