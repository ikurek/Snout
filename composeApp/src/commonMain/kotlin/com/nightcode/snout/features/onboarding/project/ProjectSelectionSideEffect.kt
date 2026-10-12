package com.nightcode.snout.features.onboarding.project

internal sealed interface ProjectSelectionSideEffect {
    data object ProjectSelected : ProjectSelectionSideEffect
}
