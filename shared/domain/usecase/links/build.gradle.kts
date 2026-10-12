plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.usecase.links"
    }

    sourceSets.commonMain.dependencies {
        api(projects.shared.domain.model.links)
        implementation(projects.shared.domain.model.common)
        implementation(projects.shared.data.links)
        implementation(projects.shared.domain.data.links)
        implementation(projects.shared.domain.usecase.appstate)
        implementation(projects.shared.domain.usecase.auth)
        implementation(projects.shared.domain.usecase.insights)
        implementation(projects.shared.domain.usecase.organization)
    }

    sourceSets.commonTest.dependencies {
        implementation(projects.shared.domain.data.appstate)
        implementation(projects.shared.domain.data.auth)
        implementation(projects.shared.domain.data.insights)
        implementation(projects.shared.domain.data.organization)
    }
}
