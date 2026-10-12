package com.nightcode.snout.shared.data.testutils

import kotlinx.serialization.json.Json

/** Configured like the app's [Json], so DTOs decode in tests exactly as they do against PostHog. */
val TestJson = Json { ignoreUnknownKeys = true }
