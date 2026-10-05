plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.annotations"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.annotations)
        implementation(projects.shared.data.annotations)
        implementation(projects.shared.domain.data.annotations)
        implementation(projects.shared.domain.data.appstate)
        implementation(projects.shared.domain.usecase.appstate)
    }
}
