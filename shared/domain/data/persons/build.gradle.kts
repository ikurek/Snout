plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.data.persons"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.persons)
        api(projects.shared.domain.model.common)
    }
}
