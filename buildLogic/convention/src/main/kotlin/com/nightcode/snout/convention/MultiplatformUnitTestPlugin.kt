package com.nightcode.snout.convention

import com.nightcode.snout.library
import com.nightcode.snout.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MultiplatformUnitTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            tasks.withType<Test>().configureEach {
                if (name == "testAndroidHostTest") useJUnitPlatform()
            }

            configure<KotlinMultiplatformExtension> {
                sourceSets.commonTest.dependencies {
                    implementation(project(":shared:common:testutils"))
                    implementation(libs.library("kotest.assertions.arrow"))
                    implementation(libs.library("kotest.assertions.core"))
                    implementation(libs.library("kotest.framework.engine"))
                    implementation(libs.library("coroutines.test"))
                }
                sourceSets.named("androidHostTest").dependencies {
                    implementation(libs.library("kotest.runner.junit5"))
                }
            }
        }
    }
}
