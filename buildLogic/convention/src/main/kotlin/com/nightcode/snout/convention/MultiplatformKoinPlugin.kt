package com.nightcode.snout.convention

import com.nightcode.snout.id
import com.nightcode.snout.library
import com.nightcode.snout.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MultiplatformKoinPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.findPlugin("koinCompiler").id)

            configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.library("koin.core"))
                    implementation(libs.library("koin.annotations"))
                }
            }
        }
    }
}
