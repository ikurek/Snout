plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.organization"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.api)
        implementation(projects.shared.api.organizations)
        implementation(projects.shared.domain.data.organization)
        implementation(projects.shared.storage.datastore)
        implementation(projects.shared.storage.database)
    }
}
