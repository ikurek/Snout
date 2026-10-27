plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.telemetry"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.telemetry)
    }
}
