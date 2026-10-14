plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.storage.database"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
    }
}

sqldelight {
    databases {
        create("SnoutDatabase") {
            packageName.set("com.nightcode.snout.shared.storage.database")
        }
    }
}
