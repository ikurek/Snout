package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import com.nightcode.snout.shared.domain.usecase.appstate.ParsePostHogLink
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class SubmitIncomingLinkTest :
    ShouldSpec({
        val repository = mock<IncomingLinkRepository>(autoUnit)
        val subject = SubmitIncomingLink(FindLinkInText(ParsePostHogLink()), repository)

        should("queue the link found in shared text") {
            subject("Check this: https://eu.posthog.com/project/1/dashboard/5")
            verifySuspend { repository.savePendingLink("https://eu.posthog.com/project/1/dashboard/5") }
        }

        should("queue nothing when the text has no link") {
            subject("no links here")
            verifySuspend(not) { repository.savePendingLink(any()) }
        }
    })
