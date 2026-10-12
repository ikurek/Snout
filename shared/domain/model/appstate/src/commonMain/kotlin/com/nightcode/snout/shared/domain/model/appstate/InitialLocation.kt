package com.nightcode.snout.shared.domain.model.appstate

/** [Login] skips the intro for someone who connected before and only lost the key (a 401 or a revoked key). */
enum class InitialLocation { Intro, Login, ProjectSelection, Home }
