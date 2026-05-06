package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

@OptIn(TestData::class)
class SaveSelectedProjectTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>(autoUnit)
        val subject = SaveSelectedProject(repository)

        should("delegate to repository with correct arguments") {
            subject(TestSelectedProject.organizationId, TestSelectedProject.projectId)
            verifySuspend {
                repository.saveSelectedProject(
                    TestSelectedProject.organizationId,
                    TestSelectedProject.projectId,
                )
            }
        }
    })
