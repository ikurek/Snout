package com.nightcode.snout.shared.common.testutils

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode

object KotestProjectConfig : AbstractProjectConfig() {
    override val isolationMode = IsolationMode.InstancePerTest
}
