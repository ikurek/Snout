import dev.detekt.gradle.Detekt

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.detekt) apply true
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.koinCompiler) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.composeScreenshot) apply false
}

detekt {
    autoCorrect = true
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/detekt.yml")
    parallel = true
    source.setFrom(projectDir)
}

tasks.withType<Detekt>().configureEach {
    exclude("**/build/**")
}

dependencies {
    detektPlugins(libs.detekt.ktlint)
}
