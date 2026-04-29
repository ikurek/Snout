plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.auth)
        api(projects.shared.domain.usecase.auth)
    }
}
