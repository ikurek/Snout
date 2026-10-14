plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.usecase.user"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.user)
        implementation(projects.shared.domain.data.user)
    }
}
