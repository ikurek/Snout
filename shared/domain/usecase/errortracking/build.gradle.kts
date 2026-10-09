plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.errortracking"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.errortracking)
        implementation(projects.shared.data.errortracking)
        implementation(projects.shared.domain.data.errortracking)
        implementation(projects.shared.domain.data.appstate)
        implementation(projects.shared.domain.usecase.appstate)
    }
}
