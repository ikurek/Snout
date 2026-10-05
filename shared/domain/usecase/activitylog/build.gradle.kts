plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.activitylog"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.activitylog)
        implementation(projects.shared.data.activitylog)
        implementation(projects.shared.domain.data.activitylog)
        implementation(projects.shared.domain.data.appstate)
        implementation(projects.shared.domain.usecase.appstate)
    }
}
