package com.nightcode.snout.features.onboarding.login

import arrow.core.Either
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.ValidateAndSaveUserAuth
import com.nightcode.snout.shared.domain.usecase.auth.ValidateInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.ValidatePersonalApiKey
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first

class LoginViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val authRepository = mock<AuthRepository>(autoUnit)
        val userRepository = mock<UserRepository>()

        fun subject(savedInstanceUrl: String? = null): LoginViewModel {
            everySuspend { authRepository.getInstanceUrl() } returns savedInstanceUrl
            return LoginViewModel(
                validateInstanceUrl = ValidateInstanceUrl(),
                validatePersonalApiKey = ValidatePersonalApiKey(),
                validateAndSaveUserAuth = ValidateAndSaveUserAuth(authRepository, userRepository),
                getInstanceUrl = GetInstanceUrl(authRepository),
            )
        }

        should("fill in the instance kept from an earlier session") {
            subject(savedInstanceUrl = PostHogCloudEuUrl).uiState.value.savedInstanceUrl shouldBe PostHogCloudEuUrl
        }

        should("clear a rejected key and unlock Connect when the instance url changes") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Left(DataError.Unauthorized)
            val viewModel = subject()

            viewModel.connect(PostHogCloudEuUrl, "phx_rejected")
            viewModel.uiState.value.personalApiKeyValidationError shouldBe
                PersonalApiKeyValidationError.ServerValidationFailed
            viewModel.uiState.value.isConnectEnabled shouldBe false
            viewModel.uiState.value.isConnecting shouldBe false

            viewModel.onInstanceUrlChanged(PostHogCloudUsUrl)
            viewModel.uiState.value.personalApiKeyValidationError shouldBe null
            viewModel.uiState.value.isConnectEnabled shouldBe true
        }

        should("keep an empty key error when the instance url changes") {
            val viewModel = subject()

            viewModel.connect(PostHogCloudEuUrl, "")
            viewModel.onInstanceUrlChanged(PostHogCloudUsUrl)
            viewModel.uiState.value.personalApiKeyValidationError shouldBe PersonalApiKeyValidationError.ApiKeyIsEmpty
        }

        should("report a failed connection that isn't about the key as a side effect") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Left(DataError.RateLimited)
            val viewModel = subject()
            val effect = async(Dispatchers.Main) { viewModel.sideEffects.first() }

            viewModel.connect(PostHogCloudEuUrl, "phx_valid")
            effect.await() shouldBe LoginSideEffect.ConnectFailed(DataError.RateLimited)
            viewModel.uiState.value.personalApiKeyValidationError shouldBe null
        }
    })
