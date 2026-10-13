package com.nightcode.snout.features.settings.libraries.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.mikepenz.aboutlibraries.entity.Library
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.common.state.toLoadState
import com.nightcode.snout.features.settings.libraries.LibraryDefinitions
import com.nightcode.snout.features.settings.libraries.loadLibraries
import com.nightcode.snout.navigation.LibraryDetailNavKey
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class LibraryDetailViewModel(
    @InjectedParam private val navKey: LibraryDetailNavKey,
    private val libraryDefinitions: LibraryDefinitions,
    private val openInBrowser: OpenInBrowser,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoadState<Library>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<Library>> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _uiState.value = LoadState.Loading
        _uiState.value = libraryDefinitions.loadLibraries()
            .flatMap { libraries ->
                libraries.firstOrNull { it.uniqueId == navKey.uniqueId }?.right() ?: DataError.NotFound.left()
            }
            .toLoadState()
    }

    fun openWebsite(url: String) = openInBrowser(url)
}
