plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.experiments"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api.experiments)
        implementation(projects.shared.data.common)
        implementation(projects.shared.domain.data.experiments)
    }

    sourceSets.commonTest.dependencies {
        implementation(projects.shared.data.testutils)
    }
}
