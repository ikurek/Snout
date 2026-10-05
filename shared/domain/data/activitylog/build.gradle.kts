plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.activitylog"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.activitylog)
        api(projects.shared.domain.model.common)
    }
}
