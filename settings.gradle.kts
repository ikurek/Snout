rootProject.name = "snout"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("buildLogic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":composeApp")
include(":shared")

// API
include(":shared:api")
include(":shared:api:organizations")
include(":shared:api:users")

// Auth
include(":shared:data:appstate")
include(":shared:data:auth")
include(":shared:data:organization")
include(":shared:data:user")
include(":shared:domain:data:appstate")
include(":shared:domain:data:auth")
include(":shared:domain:data:organization")
include(":shared:domain:data:user")
include(":shared:domain:model:appstate")
include(":shared:domain:model:auth")
include(":shared:domain:model:organization")
include(":shared:domain:model:user")
include(":shared:domain:usecase:appstate")
include(":shared:domain:usecase:auth")
include(":shared:domain:usecase:organization")
include(":shared:domain:usecase:user")

// Common
include(":shared:common:coroutines")

// Storage
include(":shared:storage:datastore")
include(":shared:storage:database")
