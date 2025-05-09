package com.raveline.leboncoinapplication.data.mappers

import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.remote.dto.AlbumDto

fun AlbumDto.toEntity() = AlbumEntity(id, albumId, title, url, thumbnailUrl)
fun AlbumEntity.toDto() = AlbumDto(albumId, id, title, url, thumbnailUrl)