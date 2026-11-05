plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.widgets"
    }

    sourceSets.commonMain.dependencies {
        // What the widgets show and call; :widgets builds its loaders on these.
        api(projects.shared.domain.model.analytics)
        api(projects.shared.domain.model.appstate)
        api(projects.shared.domain.model.common)
        api(projects.shared.domain.model.insights)
        api(projects.shared.domain.model.links)
        api(projects.shared.domain.usecase.analytics)
        api(projects.shared.domain.usecase.appstate)
        api(projects.shared.domain.usecase.insights)
        api(projects.shared.domain.usecase.links)
        // The rest of the shared layer, through CoreModule; no telemetry SDK.
        implementation(projects.shared.core)
        implementation(projects.shared.storage.datastore)
        implementation(projects.shared.telemetry)
    }
}
