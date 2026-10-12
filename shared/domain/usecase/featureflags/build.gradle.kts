plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.featureflags"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.featureflags)
        implementation(projects.shared.data.featureflags)
        implementation(projects.shared.domain.data.featureflags)
        implementation(projects.shared.domain.data.appstate)
        implementation(projects.shared.domain.usecase.appstate)
    }
}
