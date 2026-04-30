package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject

class GetSelectedProject(private val repository: AppStateRepository) {
    suspend operator fun invoke(): SelectedProject? = repository.getSelectedProject()
}
