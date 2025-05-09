package com.raveline.leboncoinapplication.di

import android.content.Context
import androidx.room.Room
import com.raveline.leboncoinapplication.data.local.AppDatabase
import com.raveline.leboncoinapplication.data.local.dao.AlbumDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "ALBUMS_DATABASE").build()

    @Provides
    fun provideAlbumDao(db: AppDatabase): AlbumDao = db.albumDao()
}