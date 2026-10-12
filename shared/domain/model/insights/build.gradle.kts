plugins {
    alias(libs.plugins.snout.multiplatform.library)
    alias(libs.plugins.snout.multiplatform.unittest)
}

kotlin {
    android {
        namespace = "com.nightcode.snout.shared.domain.model.insights"
    }
}
