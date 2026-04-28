package com.nightcode.snout.convention

import com.nightcode.snout.id
import com.nightcode.snout.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class MultiplatformComposePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply(libs.findPlugin("composeMultiplatform").id)
                apply(libs.findPlugin("composeCompiler").id)
            }
        }
    }
}
