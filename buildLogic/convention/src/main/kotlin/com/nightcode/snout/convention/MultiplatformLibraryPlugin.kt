package com.nightcode.snout.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.nightcode.snout.id
import com.nightcode.snout.library
import com.nightcode.snout.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MultiplatformLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply(libs.findPlugin("androidMultiplatformLibrary").id)
                apply(libs.findPlugin("kotlinMultiplatform").id)
                apply(libs.findPlugin("kotlinSerialization").id)
            }

            configure<KotlinMultiplatformExtension> {
                compilerOptions {
                    freeCompilerArgs.addAll("-opt-in=kotlin.uuid.ExperimentalUuidApi", "-Xexpect-actual-classes")
                }

                iosArm64()
                iosSimulatorArm64()

                extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                    compileSdk {
                        version = release(libs.findVersion("android.targetSdk").get().toString().toInt()) {
                            minorApiLevel = libs.findVersion("android.compileSdkMinor").get().toString().toInt()
                        }
                    }
                    minSdk = libs.findVersion("android.minSdk").get().toString().toInt()
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }
                    withHostTest {}
                }

                sourceSets {
                    androidMain.dependencies {
                        implementation(libs.library("coroutines.android"))
                    }
                    commonMain.dependencies {
                        implementation(project(":shared:common:testdata"))
                        implementation(libs.library("arrow.core"))
                        implementation(libs.library("coroutines.core"))
                        implementation(libs.library("kotlin.datetime"))
                        implementation(libs.library("kotlin.serialization.json"))
                    }
                }
            }
        }
    }
}
