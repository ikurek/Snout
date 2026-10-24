// Architecture tests: Konsist reads the other modules' sources from disk, so this module depends on none of them.
plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.runner.junit5)
}

tasks.test {
    useJUnitPlatform()
    // Konsist reads every module's sources, so they are this task's inputs.
    inputs.files(
        fileTree(rootDir) {
            include("**/src/**/*.kt")
            exclude("**/build/**")
        },
    ).withPropertyName("kotlinSources").withPathSensitivity(PathSensitivity.RELATIVE)
}
