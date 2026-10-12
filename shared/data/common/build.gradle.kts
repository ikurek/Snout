plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.common"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.api)
        api(projects.shared.domain.model.common)
        api(projects.shared.storage.database)
        api(projects.shared.storage.datastore)
        api(projects.shared.domain.data.appstate)
    }
}
