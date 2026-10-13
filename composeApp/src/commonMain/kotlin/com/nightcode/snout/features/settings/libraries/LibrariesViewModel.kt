package com.nightcode.snout.features.settings.libraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikepenz.aboutlibraries.entity.Library
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.common.state.toLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class LibrariesViewModel(private val libraryDefinitions: LibraryDefinitions) : ViewModel() {

    private val _uiState = MutableStateFlow<LoadState<List<Library>>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<List<Library>>> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _uiState.value = LoadState.Loading
        _uiState.value = libraryDefinitions.loadLibraries().toLoadState()
    }
}
