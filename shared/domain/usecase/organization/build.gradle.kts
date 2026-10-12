plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.organization"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.organization)
        implementation(projects.shared.domain.data.organization)
    }
}
