package com.nightcode.snout.features.onboarding

import kotlinx.serialization.Serializable

/** The three steps between a fresh install (or a logout) and Home, in pager order. */
@Serializable
internal enum class OnboardingStep { Intro, Login, ProjectSelection }
