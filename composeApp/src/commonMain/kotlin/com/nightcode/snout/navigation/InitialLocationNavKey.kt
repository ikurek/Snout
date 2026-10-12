package com.nightcode.snout.navigation

import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.onboarding.OnboardingNavKey
import com.nightcode.snout.features.onboarding.OnboardingStep
import com.nightcode.snout.shared.domain.model.appstate.InitialLocation

internal fun InitialLocation.toNavKey(): NavKey = when (this) {
    InitialLocation.Intro -> OnboardingNavKey(OnboardingStep.Intro)
    InitialLocation.Login -> OnboardingNavKey(OnboardingStep.Login)
    InitialLocation.ProjectSelection -> OnboardingNavKey(OnboardingStep.ProjectSelection)
    InitialLocation.Home -> HomeNavKey
}
