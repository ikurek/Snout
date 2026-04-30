plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.usecase.organization"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.organization)
        implementation(projects.shared.data.organization)
        implementation(projects.shared.domain.data.organization)
    }
}
