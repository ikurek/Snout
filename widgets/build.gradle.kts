// What the home-screen widgets show, ready to draw: loaded through the use cases and formatted the way the app
// formats it. Android's Glance widgets (androidApp) call it in the app's process and graph; on iOS it is the
// SnoutWidgetKit framework the widget extension links, with its own graph and no Compose.
plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.widgets"
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SnoutWidgetKit"
            isStatic = true
            binaryOption("bundleId", "com.nightcode.snout.widgetkit")
            // WidgetKit asks for timelines off the main thread; the loaders don't care which thread calls them.
            binaryOption("objcExportSuspendFunctionLaunchThreadRestriction", "none")
            // The models the widget states carry, under their own names in Swift.
            export(projects.shared.domain.model.analytics)
            export(projects.shared.domain.model.common)
            // The changes they carry, as the app writes them.
            export(projects.shared.common.format)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.domain.model.analytics)
            api(projects.shared.domain.model.common)
            api(projects.shared.common.format)
            implementation(projects.shared.domain.model.appstate)
            implementation(projects.shared.domain.model.insights)
            implementation(projects.shared.domain.model.links)
            implementation(projects.shared.domain.model.organization)
            implementation(projects.shared.domain.usecase.analytics)
            implementation(projects.shared.domain.usecase.appstate)
            implementation(projects.shared.domain.usecase.insights)
            implementation(projects.shared.domain.usecase.links)
            implementation(projects.shared.domain.usecase.organization)
        }
        iosMain.dependencies {
            // The extension's own graph: the shared layer without PostHog's SDK.
            implementation(projects.shared.widgets)
        }
        commonTest.dependencies {
            implementation(projects.shared.domain.data.analytics)
            implementation(projects.shared.domain.data.appstate)
            implementation(projects.shared.domain.data.auth)
            implementation(projects.shared.domain.data.insights)
            implementation(projects.shared.domain.data.organization)
        }
    }
}
