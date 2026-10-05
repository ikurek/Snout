plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.insights"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.insights)
        api(projects.shared.domain.model.common)
    }
}
