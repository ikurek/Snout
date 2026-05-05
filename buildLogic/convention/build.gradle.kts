plugins {
    `kotlin-dsl`
}

group = "com.nightcode.snout.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.multiplatform.gradlePlugin)
    compileOnly(libs.sqldelight.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "com.nightcode.snout.android.application"
            implementationClass = "com.nightcode.snout.convention.AndroidApplicationPlugin"
        }
        register("multiplatformLibrary") {
            id = "com.nightcode.snout.multiplatform.library"
            implementationClass = "com.nightcode.snout.convention.MultiplatformLibraryPlugin"
        }
        register("multiplatformCompose") {
            id = "com.nightcode.snout.multiplatform.compose"
            implementationClass = "com.nightcode.snout.convention.MultiplatformComposePlugin"
        }
        register("multiplatformKoin") {
            id = "com.nightcode.snout.multiplatform.koin"
            implementationClass = "com.nightcode.snout.convention.MultiplatformKoinPlugin"
        }
        register("multiplatformUnitTest") {
            id = "com.nightcode.snout.multiplatform.unittest"
            implementationClass = "com.nightcode.snout.convention.MultiplatformUnitTestPlugin"
        }
    }
}
