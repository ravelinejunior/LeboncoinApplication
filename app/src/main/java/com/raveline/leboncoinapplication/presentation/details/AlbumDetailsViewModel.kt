package com.raveline.leboncoinapplication.presentation.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raveline.leboncoinapplication.data.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val repository: AlbumRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var uiState by mutableStateOf(AlbumDetailUiState())
        private set

    private var albumId: Int? = null

    init {
        val id = savedStateHandle.get<String>("albumId")?.toIntOrNull()
        albumId = id
        if (id != null) {
            loadAlbum(id)
        } else {
            uiState = uiState.copy(error = "Unknown error", isLoading = false)
        }
    }

    private fun loadAlbum(id: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            delay(200L)
            try {
                val album = repository.getAlbumById(id)
                uiState = uiState.copy(album = album, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Unknown error", isLoading = false)
            }
        }
    }
}