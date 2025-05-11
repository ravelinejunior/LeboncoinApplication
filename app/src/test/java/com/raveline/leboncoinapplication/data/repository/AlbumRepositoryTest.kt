import com.raveline.leboncoinapplication.data.local.dao.AlbumDao
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.mappers.toDto
import com.raveline.leboncoinapplication.data.remote.api.AlbumApi
import com.raveline.leboncoinapplication.data.repository.AlbumRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class AlbumRepositoryTest {

    private lateinit var mockApi: AlbumApi
    private lateinit var mockDao: AlbumDao
    private lateinit var repository: AlbumRepository

    @BeforeTest
    fun setUp() {
        mockApi = mockk()
        mockDao = mockk()
        repository = AlbumRepository(mockApi, mockDao)
    }

    @Test
    fun getAlbums_returnsLocalData_whenLocalDataIsNotEmpty() = runTest {
        val localAlbums = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumbnailUrl1")
        )
        coEvery { mockDao.getAll() } returns localAlbums

        val result = repository.getAlbums()

        assertEquals(localAlbums, result)
        coVerify(exactly = 1) { mockDao.getAll() }
        coVerify(exactly = 0) { mockApi.getAlbums() }
    }

    @Test
    fun getAlbums_fetchesRemoteData_whenLocalDataIsEmpty() = runTest {
        val remoteAlbums = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumbnailUrl1")
        )

        coEvery { mockDao.getAll() } returnsMany listOf(emptyList(), remoteAlbums)
        coEvery { mockApi.getAlbums() } returns remoteAlbums.map { it.toDto() }
        coEvery { mockDao.insertAll(remoteAlbums) } just Runs

        val result = repository.getAlbums()

        assertEquals(remoteAlbums, result)
        coVerify(exactly = 1) { mockDao.getAll() }
        coVerify(exactly = 1) { mockApi.getAlbums() }
        coVerify(exactly = 1) { mockDao.insertAll(remoteAlbums) }
    }

    @Test
    fun getAlbums_returnsLocalData_whenRemoteFetchFails() = runTest {
        val localAlbums = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumbnailUrl1")
        )

        coEvery { mockDao.getAll() } returnsMany listOf(emptyList(), localAlbums)
        coEvery { mockApi.getAlbums() } throws RuntimeException("Network error")

        val result = repository.getAlbums()

        assertEquals(localAlbums, result)
        coVerify(exactly = 2) { mockDao.getAll() }
        coVerify(exactly = 1) { mockApi.getAlbums() }
    }

    @Test
    fun getAlbumById_returnsCorrectAlbum_whenAlbumExists() = runTest {
        val localAlbums = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumbnailUrl1"),
            AlbumEntity(2, 2, "Album 2", "url 2", "thumbnailUrl2")
        )

        coEvery { mockDao.getAll() } returns localAlbums

        val result = repository.getAlbumById(1)

        assertEquals(localAlbums[0], result)
        coVerify(exactly = 1) { mockDao.getAll() }
    }

    @Test
    fun getAlbumById_returnsNull_whenAlbumDoesNotExist() = runTest {
        val localAlbums = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumbnailUrl1")
        )

        coEvery { mockDao.getAll() } returns localAlbums

        val result = repository.getAlbumById(2)

        assertNull(result)
        coVerify(exactly = 1) { mockDao.getAll() }
    }
}
