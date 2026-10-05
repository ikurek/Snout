plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.insights"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api.insights)
        implementation(projects.shared.data.common)
        implementation(projects.shared.domain.data.insights)
        implementation(libs.kotlin.serialization.json)
    }
}
