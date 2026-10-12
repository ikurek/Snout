package com.nightcode.snout.common.state

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class LoadStateTest :
    ShouldSpec({
        should("turn a result into loaded data") {
            Either.Right("flag").toLoadState() shouldBe LoadState.Loaded("flag")
        }

        should("turn a failure into an error") {
            Either.Left(DataError.Forbidden).toLoadState() shouldBe LoadState.Error(DataError.Forbidden)
        }

        should("expose data only once loaded") {
            LoadState.Loaded("flag").dataOrNull shouldBe "flag"
            LoadState.Loading.dataOrNull shouldBe null
            LoadState.Error(DataError.NotFound).dataOrNull shouldBe null
        }
    })
