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
include(":shared:api:analytics")
include(":shared:api:annotations")
include(":shared:api:experiments")
include(":shared:api:featureflags")
include(":shared:api:insights")
include(":shared:api:organizations")
include(":shared:api:users")

// Auth
include(":shared:data:analytics")
include(":shared:data:annotations")
include(":shared:data:experiments")
include(":shared:data:appstate")
include(":shared:data:auth")
include(":shared:data:featureflags")
include(":shared:data:insights")
include(":shared:data:organization")
include(":shared:data:user")
include(":shared:data:common")
include(":shared:domain:data:analytics")
include(":shared:domain:data:annotations")
include(":shared:domain:data:experiments")
include(":shared:domain:data:appstate")
include(":shared:domain:data:auth")
include(":shared:domain:data:featureflags")
include(":shared:domain:data:insights")
include(":shared:domain:data:organization")
include(":shared:domain:data:user")
include(":shared:domain:model:analytics")
include(":shared:domain:model:annotations")
include(":shared:domain:model:experiments")
include(":shared:domain:model:appstate")
include(":shared:domain:model:auth")
include(":shared:domain:model:common")
include(":shared:domain:model:featureflags")
include(":shared:domain:model:insights")
include(":shared:domain:model:organization")
include(":shared:domain:model:user")
include(":shared:domain:usecase:analytics")
include(":shared:domain:usecase:annotations")
include(":shared:domain:usecase:experiments")
include(":shared:domain:usecase:appstate")
include(":shared:domain:usecase:auth")
include(":shared:domain:usecase:featureflags")
include(":shared:domain:usecase:insights")
include(":shared:domain:usecase:organization")
include(":shared:domain:usecase:user")

// Common
include(":shared:common:coroutines")
include(":shared:common:testdata")
include(":shared:common:testutils")

// Storage
include(":shared:storage:datastore")
include(":shared:storage:database")
