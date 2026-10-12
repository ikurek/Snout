plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.errortracking"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api.errortracking)
        implementation(projects.shared.data.common)
        implementation(projects.shared.domain.data.errortracking)
    }
}
