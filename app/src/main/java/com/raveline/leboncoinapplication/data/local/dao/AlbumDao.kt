package com.raveline.leboncoinapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity

@Dao
interface AlbumDao {
    @Query("SELECT * FROM Albums_Table")
    suspend fun getAll(): List<AlbumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<AlbumEntity>)
}