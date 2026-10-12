plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.errortracking"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.errortracking)
        implementation(projects.shared.domain.data.errortracking)
        implementation(projects.shared.domain.data.appstate)
        api(projects.shared.domain.usecase.appstate)
    }
}
