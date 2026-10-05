package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class SaveThemeModeTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>(autoUnit)
        val subject = SaveThemeMode(repository)

        should("delegate to repository") {
            subject(ThemeMode.Dark)
            verifySuspend { repository.saveThemeMode(ThemeMode.Dark) }
        }
    })
