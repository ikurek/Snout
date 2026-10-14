package com.nightcode.snout.features.settings.libraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class LibrariesViewModel(private val libraryDefinitions: LibraryDefinitions) : ViewModel() {

    private val _uiState = MutableStateFlow(LibrariesUiState())
    val uiState: StateFlow<LibrariesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _uiState.value = LibrariesUiState()
        _uiState.value = libraryDefinitions.loadLibraries().fold(
            ifLeft = { LibrariesUiState(error = it) },
            ifRight = { LibrariesUiState(libraries = it) },
        )
    }
}
