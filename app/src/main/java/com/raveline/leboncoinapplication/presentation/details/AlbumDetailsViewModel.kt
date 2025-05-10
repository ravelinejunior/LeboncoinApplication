package com.raveline.leboncoinapplication.presentation.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val repository: AlbumRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf<AlbumEntity?>(null)
        private set

    init {
        val id = savedStateHandle.get<String>("albumId")?.toIntOrNull()
        id?.let {
            loadAlbum(it)
        }
    }

    private fun loadAlbum(id: Int) {
        viewModelScope.launch {
            uiState = repository.getAlbumById(id)
        }
    }
}