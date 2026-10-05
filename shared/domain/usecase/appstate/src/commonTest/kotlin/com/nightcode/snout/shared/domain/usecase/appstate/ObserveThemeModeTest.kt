package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class ObserveThemeModeTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>()
        val subject = ObserveThemeMode(repository)

        should("emit repository theme mode") {
            every { repository.observeThemeMode() } returns flowOf(ThemeMode.Light)
            subject().first() shouldBe ThemeMode.Light
        }
    })
