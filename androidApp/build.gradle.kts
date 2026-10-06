plugins {
    alias(libs.plugins.snout.android.application)
    alias(libs.plugins.composeScreenshot)
}

android {
    namespace = "com.nightcode.snout"

    defaultConfig {
        applicationId = "com.nightcode.snout"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
    screenshotTestImplementation(libs.compose.uiTooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
}
