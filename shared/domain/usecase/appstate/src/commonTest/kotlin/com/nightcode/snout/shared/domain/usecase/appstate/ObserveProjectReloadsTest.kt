package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@OptIn(TestData::class)
class ObserveProjectReloadsTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>()
        val subject = ObserveProjectReloads(repository)
        val otherProject = TestSelectedProject.copy(projectId = 2L)
        val projectChanged = ProjectReload(projectChanged = true)
        val dataChanged = ProjectReload(projectChanged = false)

        should("reload once per selected project, skipping no selection and the same project again") {
            every { repository.observeSelectedProject() } returns flowOf(
                null,
                TestSelectedProject,
                TestSelectedProject,
                TestSelectedProject.copy(organizationId = Uuid.parse("00000000-0000-0000-0000-000000000002")),
                otherProject,
                null,
                otherProject,
                TestSelectedProject,
            )
            subject().toList() shouldBe listOf(projectChanged, projectChanged, projectChanged)
        }

        should("not reload before a project is selected") {
            every { repository.observeSelectedProject() } returns flowOf(null)
            subject().toList().shouldBeEmpty()
            every { repository.observeSelectedProject() } returns emptyFlow()
            subject().toList().shouldBeEmpty()
        }

        should("reload at once, again for the same project after a change and for each newly selected project") {
            val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)
            every { repository.observeSelectedProject() } returns selected
            val changes = MutableSharedFlow<Unit>()
            val reloads = MutableStateFlow(emptyList<ProjectReload>())
            coroutineScope {
                val collector = launch { subject(changes).collect { reload -> reloads.update { it + reload } } }
                changes.subscriptionCount.first { it > 0 }
                changes.emit(Unit)
                reloads.first { it.size == 2 }
                selected.value = otherProject
                reloads.first { it.size == 3 } shouldBe listOf(projectChanged, dataChanged, projectChanged)
                collector.cancel()
            }
        }
    })
