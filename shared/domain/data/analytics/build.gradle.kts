plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.analytics"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.analytics)
        api(projects.shared.domain.model.common)
    }
}
