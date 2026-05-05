plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.usecase.user"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.user)
        implementation(projects.shared.data.user)
        implementation(projects.shared.domain.data.user)
    }
}
