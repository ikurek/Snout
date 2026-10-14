plugins {
    `kotlin-dsl`
}

group = "com.nightcode.snout.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.multiplatform.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("multiplatformLibrary") {
            id = "com.nightcode.snout.multiplatform.library"
            implementationClass = "com.nightcode.snout.convention.MultiplatformLibraryPlugin"
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
