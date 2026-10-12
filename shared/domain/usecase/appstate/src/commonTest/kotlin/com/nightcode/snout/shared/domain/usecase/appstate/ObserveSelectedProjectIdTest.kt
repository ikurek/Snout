package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlin.uuid.Uuid

@OptIn(TestData::class)
class ObserveSelectedProjectIdTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>()
        val subject = ObserveSelectedProjectId(repository)
        val otherProject = TestSelectedProject.copy(projectId = 2L)

        should("emit each selected project id once, skipping no selection") {
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
            subject().toList() shouldBe listOf(1L, 2L, 1L)
        }

        should("emit nothing while no project is selected") {
            every { repository.observeSelectedProject() } returns flowOf(null)
            subject().toList().shouldBeEmpty()
        }

        should("switch to the newly selected project's flow") {
            every { repository.observeSelectedProject() } returns flow {
                emit(TestSelectedProject)
                delay(SwitchDelayMillis)
                emit(otherProject)
                awaitCancellation()
            }
            subject.flatMapProject { projectId ->
                flow {
                    emit("project $projectId")
                    awaitCancellation()
                }
            }.take(2).toList() shouldBe listOf("project 1", "project 2")
        }

        should("not observe anything before a project is selected") {
            every { repository.observeSelectedProject() } returns flowOf(null)
            subject.flatMapProject<String> { error("observed project $it") }.toList().shouldBeEmpty()
            every { repository.observeSelectedProject() } returns emptyFlow()
            subject.flatMapProject<String> { error("observed project $it") }.toList().shouldBeEmpty()
        }
    })

private const val SwitchDelayMillis = 10L
