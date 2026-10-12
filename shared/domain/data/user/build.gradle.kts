plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.user"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.common)
        api(projects.shared.domain.model.user)
    }
}
