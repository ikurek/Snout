package com.nightcode.snout.convention

import com.android.build.api.dsl.ApplicationExtension
import com.nightcode.snout.id
import com.nightcode.snout.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply(libs.findPlugin("androidApplication").id)
                apply(libs.findPlugin("composeCompiler").id)
            }

            configure<ApplicationExtension> {
                compileSdk = libs.findVersion("android.targetSdk").get().toString().toInt()

                defaultConfig {
                    minSdk = libs.findVersion("android.minSdk").get().toString().toInt()
                    targetSdk = libs.findVersion("android.targetSdk").get().toString().toInt()
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
            }
        }
    }
}
