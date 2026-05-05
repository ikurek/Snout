package com.nightcode.snout.shared.domain.model.auth

import com.nightcode.snout.shared.common.testdata.TestData

data class PersonalApiKey(val value: String)

@TestData
val TestPersonalApiKey = PersonalApiKey(value = "phx_test1234567890abcdef")
