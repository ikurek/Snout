import com.mikepenz.aboutlibraries.plugin.StrictMode

plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.compose)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.composeapp"
        androidResources { enable = true }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.nightcode.snout.composeapp")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(libs.aboutlibraries.core)
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.lifecycle.viewmodel)
            implementation(libs.compose.lifecycle.runtime)
            implementation(libs.compose.navigation3.ui)
            implementation(libs.compose.navigationevent)
            implementation(libs.compose.lifecycle.viewmodel.navigation3)
            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            // Koin
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
        }
        commonTest.dependencies {
            implementation(projects.shared.domain.data.activitylog)
            implementation(projects.shared.domain.data.analytics)
            implementation(projects.shared.domain.data.annotations)
            implementation(projects.shared.domain.data.errortracking)
            implementation(projects.shared.domain.data.experiments)
            implementation(projects.shared.domain.data.appstate)
            implementation(projects.shared.domain.data.auth)
            implementation(projects.shared.domain.data.featureflags)
            implementation(projects.shared.domain.data.insights)
            implementation(projects.shared.domain.data.links)
            implementation(projects.shared.domain.data.logs)
            implementation(projects.shared.domain.data.organization)
            implementation(projects.shared.domain.data.user)
        }
        named("androidHostTest").dependencies {
            implementation(libs.koin.test)
        }
    }
}

// Open-source licenses for the Libraries screen. The export collects this module's non-test classpaths (Android
// runtime plus the common and iOS metadata, so :shared's dependencies are included) offline, with the license texts
// from aboutlibraries/licenses. A license without a text there fails the build: add its file and allow it below.
val libraryDefinitionsDir = "generated/aboutLibraries/composeResources"

aboutLibraries {
    offlineMode = true
    collect {
        configPath = file("aboutlibraries")
        includePlatform = false
    }
    export {
        outputFile = layout.buildDirectory.file("$libraryDefinitionsDir/files/aboutlibraries.json")
    }
    library {
        mergePlatformArtifacts = true
    }
    license {
        strictMode = StrictMode.FAIL
        allowedLicenses.addAll("Apache-2.0", "BSD-3-Clause", "MIT")
    }
}

// The export is generated on every build rather than committed: the platform source sets' compose resources come
// from its output directory, so Android and iOS both package files/aboutlibraries.json and Gradle runs the export
// before their resources are prepared. commonMain keeps its own src/commonMain/composeResources.
val libraryDefinitions = tasks.named("exportLibraryDefinitions").map {
    layout.buildDirectory.dir(libraryDefinitionsDir).get()
}
compose.resources {
    customDirectory("androidMain", libraryDefinitions)
    customDirectory("iosMain", libraryDefinitions)
}

composeCompiler {
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("compose_stability_config.conf"),
    )
}

dependencies {
    // Runtime-only tooling so Android Studio can render @Preview composables from this module.
    androidRuntimeClasspath(libs.compose.uiTooling)
}
