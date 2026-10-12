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
import kotlinx.coroutines.launch

class LoginViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val authRepository = mock<AuthRepository>(autoUnit)
        val userRepository = mock<UserRepository>()

        fun subject() = LoginViewModel(
            validateInstanceUrl = ValidateInstanceUrl(),
            validatePersonalApiKey = ValidatePersonalApiKey(),
            validateAndSaveUserAuth = ValidateAndSaveUserAuth(authRepository, userRepository),
            getInstanceUrl = GetInstanceUrl(authRepository),
        )

        should("clear a rejected key and unlock Connect when the instance url changes") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Left(DataError.Unauthorized)
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.isConnectEnabled.collect {} }

            viewModel.onConnectClick(PostHogCloudEuUrl, "phx_rejected")
            viewModel.personalApiKeyValidationError.value shouldBe PersonalApiKeyValidationError.ServerValidationFailed
            viewModel.isConnectEnabled.value shouldBe false

            viewModel.onInstanceUrlChange(PostHogCloudUsUrl)
            viewModel.personalApiKeyValidationError.value shouldBe null
            viewModel.isConnectEnabled.value shouldBe true
            collector.cancel()
        }

        should("keep an empty key error when the instance url changes") {
            val viewModel = subject()

            viewModel.onConnectClick(PostHogCloudEuUrl, "")
            viewModel.onInstanceUrlChange(PostHogCloudUsUrl)
            viewModel.personalApiKeyValidationError.value shouldBe PersonalApiKeyValidationError.ApiKeyIsEmpty
        }
    })
