plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.model.telemetry"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.analytics)
        api(projects.shared.domain.model.appstate)
        api(projects.shared.domain.model.errortracking)
        api(projects.shared.domain.model.insights)
    }
}
