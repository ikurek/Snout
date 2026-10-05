plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.featureflags"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.featureflags)
        api(projects.shared.domain.model.common)
    }
}
