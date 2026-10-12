package com.nightcode.snout.shared.data.testutils

import com.nightcode.snout.shared.api.ApiJson
import kotlinx.serialization.json.Json

/** The app's [Json], so DTOs encode and decode in tests exactly as they do against PostHog. */
val TestJson: Json = ApiJson
