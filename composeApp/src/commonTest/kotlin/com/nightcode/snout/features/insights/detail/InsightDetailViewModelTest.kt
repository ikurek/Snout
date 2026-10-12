package com.nightcode.snout.features.insights.detail

import arrow.core.Either
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.model.insights.TestInsightDetail
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.GetInsightDetail
import com.nightcode.snout.shared.domain.usecase.insights.SetInsightFavorite
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first

@OptIn(TestData::class)
class InsightDetailViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<InsightRepository>()
        val insightId = TestInsightDetail.summary.id
        // The fixture is favorited, so a toggle unfavorites it.
        val unfavorited = TestInsightDetail.copy(summary = TestInsightDetail.summary.copy(favorited = false))

        fun subject(): InsightDetailViewModel {
            val projectId = GetSelectedProjectId(appStateRepository)
            return InsightDetailViewModel(
                insightId = insightId,
                getInsightDetail = GetInsightDetail(projectId, repository),
                setInsightFavorite = SetInsightFavorite(projectId, repository),
            )
        }

        beforeTest { everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject }

        should("load the insight on start") {
            everySuspend { repository.getInsight(any(), insightId) } returns Either.Right(TestInsightDetail)
            subject().uiState.value shouldBe LoadState.Loaded(TestInsightDetail)
        }

        should("show the favorite change before the server confirms it") {
            val response = CompletableDeferred<Either<DataError, InsightSummary>>()
            everySuspend { repository.getInsight(any(), insightId) } returns Either.Right(TestInsightDetail)
            everySuspend { repository.setFavorite(TestSelectedProject.projectId, insightId, false) } calls
                { response.await() }
            val viewModel = subject()

            val toggle = viewModel.toggleFavorite()
            viewModel.uiState.value shouldBe LoadState.Loaded(unfavorited)

            response.complete(Either.Right(unfavorited.summary))
            toggle.join()
            viewModel.uiState.value shouldBe LoadState.Loaded(unfavorited)
        }

        should("roll the favorite back and report when the server rejects it") {
            everySuspend { repository.getInsight(any(), insightId) } returns Either.Right(TestInsightDetail)
            everySuspend { repository.setFavorite(any(), insightId, false) } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()

            viewModel.toggleFavorite()

            viewModel.sideEffects.first() shouldBe InsightDetailSideEffect.FavoriteUpdateFailed
            viewModel.uiState.value shouldBe LoadState.Loaded(TestInsightDetail)
        }

        should("ignore the favorite toggle until the insight has loaded") {
            everySuspend { repository.getInsight(any(), insightId) } returns Either.Left(DataError.ConnectionError)
            val viewModel = subject()

            viewModel.toggleFavorite().join()

            viewModel.uiState.value shouldBe LoadState.Error(DataError.ConnectionError)
            verifySuspend(VerifyMode.not) { repository.setFavorite(any(), any(), any()) }
        }
    })
