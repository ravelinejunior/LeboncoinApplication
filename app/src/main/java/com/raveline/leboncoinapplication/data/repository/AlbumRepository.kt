package com.raveline.leboncoinapplication.data.repository

import com.raveline.leboncoinapplication.data.local.dao.AlbumDao
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.mappers.toEntity
import com.raveline.leboncoinapplication.data.remote.api.AlbumApi
import javax.inject.Inject

class AlbumRepository @Inject constructor(
    private val api: AlbumApi,
    private val dao: AlbumDao
) {

    suspend fun getAlbums(): List<AlbumEntity> {
        return try {
            val localData = dao.getAll()
            localData.ifEmpty {
                val remote = api.getAlbums()
                val entities = remote.map { it.toEntity() }
                dao.insertAll(entities)
                entities
            }
        } catch (e: Exception) {
            dao.getAll()
        }
    }

    suspend fun getAlbumById(id: Int): AlbumEntity? = dao.getAll().find { it.id == id }
}