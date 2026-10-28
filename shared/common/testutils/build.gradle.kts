plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.nightcode.snout.shared.common.testutils"
        compileSdk {
            version = release(libs.versions.android.targetSdk.get().toInt()) {
                minorApiLevel = libs.versions.android.compileSdkMinor.get().toInt()
            }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets.commonMain.dependencies {
        implementation(libs.kotest.framework.engine)
        implementation(libs.coroutines.core)
        implementation(libs.coroutines.test)
    }
}
