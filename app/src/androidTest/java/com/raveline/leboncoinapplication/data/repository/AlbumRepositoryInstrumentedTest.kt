package com.raveline.leboncoinapplication.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raveline.leboncoinapplication.data.local.AppDatabase
import com.raveline.leboncoinapplication.data.local.dao.AlbumDao
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.mappers.toDto
import com.raveline.leboncoinapplication.data.remote.api.AlbumApi
import com.raveline.leboncoinapplication.data.remote.dto.AlbumDto
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AlbumRepositoryInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AlbumDao
    private lateinit var fakeApi: AlbumApi
    private lateinit var repository: AlbumRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.albumDao()
        fakeApi = FakeAlbumApi()
        repository = AlbumRepository(fakeApi, dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAlbums_fetchesFromApiAndStoresInDb_whenLocalIsEmpty() = runBlocking {
        val result = repository.getAlbums()

        assertEquals(2, result.size)
        val fromDb = dao.getAll()
        assertEquals(result, fromDb)
    }

    @Test
    fun getAlbums_returnsLocal_whenDataExists() = runBlocking {
        val album = AlbumEntity(1, 1, "Album Local", "url", "thumb")
        dao.insertAll(listOf(album))

        val result = repository.getAlbums()

        assertEquals(1, result.size)
        assertEquals(album, result[0])
    }

    @Test
    fun getAlbumById_returnsCorrectAlbum() = runBlocking {
        val album = AlbumEntity(2, 1, "Album Test", "url2", "thumb2")
        dao.insertAll(listOf(album))

        val result = repository.getAlbumById(2)

        assertNotNull(result)
        assertEquals(album, result)
    }

    @Test
    fun getAlbumsFromApi_alwaysFetchesAndOverwritesDb() = runBlocking {
        val oldAlbums = listOf(
            AlbumEntity(9, 9, "Old Album", "urlOld", "thumbOld")
        )
        dao.insertAll(oldAlbums)

        val newAlbums = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumb1"),
            AlbumEntity(2, 2, "Album 2", "url2", "thumb2")
        )

        (fakeApi as FakeAlbumApi).setAlbums(newAlbums.map { it.toDto() })

        val result = repository.getAlbumsFromApi()

        assertEquals(newAlbums, result)

        val fromDb = dao.getAll()
        assertTrue(fromDb.containsAll(newAlbums))

        assertTrue(fromDb.size >= newAlbums.size)
    }

    @Test
    fun getAlbums_returnsEmptyList_whenApiFailsAndLocalIsEmpty() = runBlocking {
        (fakeApi as FakeAlbumApi).setThrowError(true)

        val result = repository.getAlbums()

        assertTrue(result.isEmpty())
        val fromDb = dao.getAll()
        assertTrue(fromDb.isEmpty())
    }

    @Test
    fun getAlbumsFromApi_returnsEmptyList_whenApiFails() = runBlocking {
        (fakeApi as FakeAlbumApi).setThrowError(true)

        val result = repository.getAlbumsFromApi()

        assertTrue(result.isEmpty())
        val fromDb = dao.getAll()
        assertTrue(fromDb.isEmpty())
    }

    @Test
    fun getAlbumById_returnsNull_whenNotFound() = runBlocking {
        val result = repository.getAlbumById(123)
        assertTrue(result == null)
    }
}

class FakeAlbumApi : AlbumApi {
    private var albums: List<AlbumDto> = listOf(
        AlbumDto(1, 1, "Album 1", "url1", "thumb1"),
        AlbumDto(2, 2, "Album 2", "url2", "thumb2")
    )

    private var throwError: Boolean = false

    override suspend fun getAlbums(): List<AlbumDto> {
        if (throwError) throw RuntimeException("Simulated API error")
        return albums
    }

    fun setAlbums(newAlbums: List<AlbumDto>) {
        albums = newAlbums
    }

    fun setThrowError(shouldThrow: Boolean) {
        throwError = shouldThrow
    }
}


