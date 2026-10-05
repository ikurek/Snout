package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository

class GetInstanceUrl(private val repository: AuthRepository) {
    suspend operator fun invoke(): String? = repository.getInstanceUrl()
}
