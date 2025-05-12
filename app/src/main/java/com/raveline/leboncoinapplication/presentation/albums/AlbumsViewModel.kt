package com.raveline.leboncoinapplication.presentation.albums

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raveline.leboncoinapplication.domain.use_case.GetAlbumsUseCase
import com.raveline.leboncoinapplication.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    application: Application
) : ViewModel() {

    var uiState by mutableStateOf(AlbumsUiState())
        private set

    init {
        observeNetwork(application)
        loadAlbums()
    }

    internal fun loadAlbums() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val result = getAlbumsUseCase()
                uiState = uiState.copy(isLoading = false, albums = result)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun observeNetwork(context: Context) {
        NetworkMonitor.observe(context).onEach { isConnected ->
            if (isConnected && uiState.albums.isEmpty()) {
                loadAlbums()
            }
        }.launchIn(viewModelScope)
    }
}