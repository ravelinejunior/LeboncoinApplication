package com.raveline.leboncoinapplication.domain.use_case

import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.repository.AlbumRepository
import javax.inject.Inject

open class GetAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    open suspend operator fun invoke(forceRefresh: Boolean = false): List<AlbumEntity> =
        if (forceRefresh) repository.getAlbumsFromApi()
        else repository.getAlbums()
}