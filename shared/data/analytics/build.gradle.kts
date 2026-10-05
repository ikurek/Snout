plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.analytics"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api.analytics)
        implementation(projects.shared.data.common)
        implementation(projects.shared.domain.data.analytics)
        implementation(libs.kotlin.serialization.json)
    }
}
