plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.organization"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.organization)
    }
}
