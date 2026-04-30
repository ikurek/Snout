plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.appstate"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.appstate)
    }
}
