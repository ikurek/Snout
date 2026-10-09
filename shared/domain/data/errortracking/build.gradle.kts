plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.errortracking"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.errortracking)
        api(projects.shared.domain.model.common)
    }
}
