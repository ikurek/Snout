plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.annotations"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.annotations)
        api(projects.shared.domain.model.common)
    }
}
