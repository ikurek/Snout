plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.user"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.api.users)
            implementation(projects.shared.domain.data.user)
        }
    }
}
