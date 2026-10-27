import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.koin)
    alias(libs.plugins.snout.multiplatform.unittest)
    alias(libs.plugins.buildKonfig)
}

/**
 * A PostHog setting from the environment (Codemagic's `posthog` group) or local.properties, or an empty string. A
 * build without them (a contributor's, a fork's, F-Droid's) is complete: it just never starts PostHog. Xcode's
 * framework build phase runs Gradle too, so the iOS app gets the same values, through BuildKonfig's `PostHogSettings`.
 */
fun postHogSetting(variable: String, property: String, isValid: (String) -> Boolean): String {
    val localProperties = rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.let { file -> Properties().apply { file.inputStream().use(::load) } }
    val value = providers.environmentVariable(variable).orNull?.takeIf { it.isNotBlank() }
        ?: localProperties?.getProperty(property)?.takeIf { it.isNotBlank() }
        ?: return ""
    require(isValid(value)) { "Invalid PostHog configuration in $variable ($property): $value" }
    return value
}

buildkonfig {
    packageName = "com.nightcode.snout.shared.telemetry.sdk"
    objectName = "PostHogSettings"

    defaultConfigs {
        val projectToken = postHogSetting("POSTHOG_PROJECT_TOKEN", "posthog.project.token") { it.startsWith("phc_") }
        val host = postHogSetting("POSTHOG_HOST", "posthog.host") { it.startsWith("https://") }
        buildConfigField(STRING, "projectToken", projectToken)
        buildConfigField(STRING, "host", host)
    }
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.telemetry.sdk"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.telemetry)
            // On iOS, posthog-kmp brings posthog-ios through Kotlin's SwiftPM import; the app links it through
            // iosApp/KotlinMultiplatformLinkedPackage.
            implementation(libs.posthog.kmp)
        }
    }
}
