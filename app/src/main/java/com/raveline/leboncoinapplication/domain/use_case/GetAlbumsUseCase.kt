package com.raveline.leboncoinapplication.domain.use_case

import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.repository.AlbumRepository
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    /**
     * @param forceRefresh se true, busca sempre da API; senão, tenta cache primeiro.
     */
    suspend operator fun invoke(forceRefresh: Boolean = false): List<AlbumEntity> =
        if (forceRefresh) repository.getAlbumsFromApi()
        else repository.getAlbums()
}