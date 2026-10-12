plugins {
    alias(libs.plugins.snout.multiplatform.library)
}

// Fakes and helpers shared by the data modules' tests; only ever a commonTest dependency.
kotlin {
    android {
        namespace = "com.nightcode.snout.shared.data.testutils"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.data.common)
        api(libs.ktor.client.mock)
        implementation(libs.datastore)
        implementation(libs.datastore.preferences)
        implementation(libs.ktor.client.contentNegotiation)
        implementation(libs.ktor.serialization.json)
    }
}
