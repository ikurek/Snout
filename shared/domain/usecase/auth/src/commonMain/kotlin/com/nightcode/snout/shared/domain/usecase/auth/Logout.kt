package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import org.koin.core.annotation.Single

@Single
class Logout(private val repository: AuthRepository, private val clearLocalData: ClearLocalData) {
    suspend operator fun invoke() {
        clearLocalData()
        repository.clear()
    }
}
