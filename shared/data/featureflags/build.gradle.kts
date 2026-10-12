plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.featureflags"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api.featureflags)
        implementation(projects.shared.data.common)
        implementation(projects.shared.domain.data.featureflags)
    }

    sourceSets.commonTest.dependencies {
        implementation(projects.shared.data.testutils)
    }
}
