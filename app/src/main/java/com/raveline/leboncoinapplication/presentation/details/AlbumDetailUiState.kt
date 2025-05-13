package com.raveline.leboncoinapplication.presentation.details

import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity

data class AlbumDetailUiState(
    val isLoading: Boolean = false,
    val album: AlbumEntity? = null,
    val error: String? = null
)
