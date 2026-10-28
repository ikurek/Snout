import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeScreenshot)
}

android {
    namespace = "com.nightcode.snout"
    compileSdk {
        version = release(libs.versions.android.targetSdk.get().toInt()) {
            minorApiLevel = libs.versions.android.compileSdkMinor.get().toInt()
        }
    }

    defaultConfig {
        applicationId = "com.nightcode.snout"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Codemagic passes the release tag (major.minor.patch); the version code follows from it, so every build of a
        // tag, wherever it runs, gets the same code.
        val version = providers.gradleProperty("versionName").orNull ?: "0.0.1"
        versionName = version
        versionCode = versionCodeOf(version)
    }

    signingConfigs {
        // The upload key: Codemagic provides it from its code signing identities, and a machine that has it names it
        // in the git-ignored local.properties: signing.storeFile (relative to the repository root),
        // signing.storePassword, signing.keyAlias and signing.keyPassword. Release builds are unsigned anywhere else.
        val localProperties = rootProject.file("local.properties")
            .takeIf { it.exists() }
            ?.let { file -> Properties().apply { file.inputStream().use(::load) } }
        val ciKeystorePath = providers.environmentVariable("CM_KEYSTORE_PATH").orNull
        if (ciKeystorePath != null) {
            create("release") {
                storeFile = file(ciKeystorePath)
                storePassword = providers.environmentVariable("CM_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("CM_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("CM_KEY_PASSWORD").get()
            }
        } else if (localProperties?.getProperty("signing.storeFile") != null) {
            create("release") {
                storeFile = rootProject.file(localProperties.getProperty("signing.storeFile"))
                storePassword = localProperties.getProperty("signing.storePassword")
                keyAlias = localProperties.getProperty("signing.keyAlias")
                keyPassword = localProperties.getProperty("signing.keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // The screenshot test plugin refuses to apply unless this and the same flag in gradle.properties are both set.
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

// Previews show times in the system time zone, so screenshots render in UTC to come out the same on every machine.
tasks.withType<Test>().matching { it.name.endsWith("ScreenshotTest") }.configureEach {
    systemProperty("user.timezone", "UTC")
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
    screenshotTestImplementation(libs.compose.uiTooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
}

/** `major.minor.patch` as `major * 10000 + minor * 100 + patch`, so 1.2.3 is 10203; minor and patch stay under 100. */
fun versionCodeOf(version: String): Int {
    val parts = requireNotNull(Regex("""(\d+)\.(\d{1,2})\.(\d{1,2})""").matchEntire(version)) {
        "versionName must be major.minor.patch with minor and patch under 100, was $version"
    }.destructured.toList().map(String::toInt)
    return parts[0] * 10_000 + parts[1] * 100 + parts[2]
}
