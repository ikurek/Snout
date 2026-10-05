plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.compose)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.mokkery)
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
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            implementation(projects.shared)
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
            implementation(libs.compose.lifecycle.viewmodel.navigation3)
            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            // Koin
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            // Kotlin
            implementation(libs.kotlin.serialization.json)
        }
        commonTest.dependencies {
            implementation(projects.shared.domain.data.analytics)
            implementation(projects.shared.domain.data.annotations)
            implementation(projects.shared.domain.data.experiments)
            implementation(projects.shared.domain.data.appstate)
            implementation(projects.shared.domain.data.auth)
            implementation(projects.shared.domain.data.featureflags)
            implementation(projects.shared.domain.data.insights)
            implementation(projects.shared.domain.data.organization)
            implementation(projects.shared.domain.data.user)
        }
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("compose_stability_config.conf"),
    )
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
