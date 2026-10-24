package com.nightcode.snout.konsist

import io.kotest.core.test.TestScope

/** The running test's name, for Konsist's failure message (it would otherwise name a coroutine frame). */
val TestScope.rule: String get() = testCase.name.name
