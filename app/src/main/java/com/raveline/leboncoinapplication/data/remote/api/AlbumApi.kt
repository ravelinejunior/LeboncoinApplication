package com.raveline.leboncoinapplication.data.remote.api

import com.raveline.leboncoinapplication.data.remote.dto.AlbumDto
import retrofit2.http.GET

interface AlbumApi {
    @GET("shared/technical-test.json")
    suspend fun getAlbums(): List<AlbumDto>
}