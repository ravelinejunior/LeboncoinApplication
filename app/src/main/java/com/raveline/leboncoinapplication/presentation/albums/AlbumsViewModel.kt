package com.raveline.leboncoinapplication.presentation.albums

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raveline.leboncoinapplication.domain.use_case.GetAlbumsUseCase
import com.raveline.leboncoinapplication.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class AlbumsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val savedStateHandle: SavedStateHandle,
    application: Application
) : ViewModel() {

    var isGrid by mutableStateOf(savedStateHandle[KEY_IS_GRID] ?: false)
        private set

    var uiState by mutableStateOf(AlbumsUiState())
        private set

    init {
        observeNetwork(application)
        refreshAlbums()
    }

    fun refreshAlbums(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val result = getAlbumsUseCase(forceRefresh)
                uiState = uiState.copy(isLoading = false, albums = result, error = null)
                savedStateHandle[KEY_LAST_LOAD_SUCCESS] = System.currentTimeMillis()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeNetwork(context: Context) {
        NetworkMonitor.observe(context)
            .filter { it }
            .debounce(1_000)
            .onEach { refreshAlbums(forceRefresh = true) }
            .launchIn(viewModelScope)
    }

    fun toggleLayout() {
        isGrid = !isGrid
        savedStateHandle[KEY_IS_GRID] = isGrid
    }

    companion object {
        private const val KEY_IS_GRID = "is_grid_layout"
        private const val KEY_LAST_LOAD_SUCCESS = "last_load_success"
    }
}