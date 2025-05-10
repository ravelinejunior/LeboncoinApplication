package com.raveline.leboncoinapplication.presentation.albums

import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity

data class AlbumsUiState(
    val isLoading: Boolean = false,
    val albums: List<AlbumEntity> = emptyList(),
    val error: String? = null
)
