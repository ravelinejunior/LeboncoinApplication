import com.raveline.leboncoinapplication.data.local.dao.AlbumDao
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.mappers.toEntity
import com.raveline.leboncoinapplication.data.remote.api.AlbumApi
import com.raveline.leboncoinapplication.data.remote.dto.AlbumDto
import com.raveline.leboncoinapplication.data.repository.AlbumRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
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
    fun `getAlbums returns local data if not empty`() = runTest {
        val localAlbums = listOf(AlbumEntity(1, 1, "Album 1", "url1", "thumb1"))
        coEvery { mockDao.getAll() } returns localAlbums

        val result = repository.getAlbums()

        assertEquals(localAlbums, result)
        coVerify(exactly = 1) { mockDao.getAll() }
        coVerify(exactly = 0) { mockApi.getAlbums() }
    }

    @Test
    fun `getAlbums fetches and saves remote data if local is empty`() = runTest {
        val remoteAlbumsDto = listOf(
            AlbumDto(
                1,
                1,
                "Album 1",
                "url1",
                "thumb1"
            )
        )
        val remoteAlbums = remoteAlbumsDto.map { it.toEntity() }

        coEvery { mockDao.getAll() } returns emptyList()
        coEvery { mockApi.getAlbums() } returns remoteAlbumsDto
        coEvery { mockDao.insertAll(remoteAlbums) } just Runs

        val result = repository.getAlbums()

        assertEquals(remoteAlbums, result)
        coVerifySequence {
            mockDao.getAll()
            mockApi.getAlbums()
            mockDao.insertAll(remoteAlbums)
        }
    }

    @Test
    fun `getAlbums returns local data on API error`() = runTest {
        val fallbackLocalAlbums = listOf(AlbumEntity(1, 1, "Album 1", "url1", "thumb1"))

        coEvery { mockDao.getAll() } returnsMany listOf(emptyList(), fallbackLocalAlbums)
        coEvery { mockApi.getAlbums() } throws RuntimeException("Network error")

        val result = repository.getAlbums()

        assertEquals(fallbackLocalAlbums, result)
        coVerify(exactly = 2) { mockDao.getAll() }
        coVerify(exactly = 1) { mockApi.getAlbums() }
    }

    @Test
    fun `getAlbumsFromApi fetches and stores remote data`() = runTest {
        val remoteAlbumsDto = listOf(
            AlbumDto(
                2,
                2,
                "Album 2",
                "url2",
                "thumb2"
            )
        )
        val remoteAlbums = remoteAlbumsDto.map { it.toEntity() }

        coEvery { mockApi.getAlbums() } returns remoteAlbumsDto
        coEvery { mockDao.insertAll(remoteAlbums) } just Runs

        val result = repository.getAlbumsFromApi()

        assertEquals(remoteAlbums, result)
        coVerifySequence {
            mockApi.getAlbums()
            mockDao.insertAll(remoteAlbums)
        }
    }

    @Test
    fun `getAlbumsFromApi returns empty list on error`() = runTest {
        coEvery { mockApi.getAlbums() } throws RuntimeException("API down")

        val result = repository.getAlbumsFromApi()

        assertEquals(emptyList(), result)
        coVerify(exactly = 1) { mockApi.getAlbums() }
    }

    @Test
    fun `getAlbumById returns album if exists`() = runTest {
        val albums = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumb1"),
            AlbumEntity(2, 2, "Album 2", "url2", "thumb2")
        )
        coEvery { mockDao.getAll() } returns albums

        val result = repository.getAlbumById(2)

        assertEquals(albums[1], result)
        coVerify { mockDao.getAll() }
    }

    @Test
    fun `getAlbumById returns null if album doesn't exist`() = runTest {
        val albums = listOf(AlbumEntity(1, 1, "Album 1", "url1", "thumb1"))
        coEvery { mockDao.getAll() } returns albums

        val result = repository.getAlbumById(99)

        assertNull(result)
        coVerify { mockDao.getAll() }
    }


    @Test
    fun `getAlbums returns empty list if local and remote both fail`() = runTest {
        coEvery { mockDao.getAll() } returns emptyList()
        coEvery { mockApi.getAlbums() } throws RuntimeException("API fail")
        coEvery { mockDao.getAll() } returns emptyList()

        val result = repository.getAlbums()

        assertEquals(emptyList(), result)
        coVerify(exactly = 2) { mockDao.getAll() }
        coVerify { mockApi.getAlbums() }
    }

    @Test
    fun `getAlbumsFromApi returns empty list if API and insertAll both throw`() = runTest {
        coEvery { mockApi.getAlbums() } throws RuntimeException("API crashed")

        val result = repository.getAlbumsFromApi()

        assertEquals(emptyList(), result)
        coVerify(exactly = 1) { mockApi.getAlbums() }
    }

    @Test
    fun `getAlbumById handles exception from dao gracefully`() = runTest {
        coEvery { mockDao.getAll() } throws RuntimeException("DAO crashed")

        val result = runCatching {
            repository.getAlbumById(1)
        }

        assert(result.isFailure)
        coVerify { mockDao.getAll() }
    }

    @Test
    fun `getAlbums only calls insertAll once on success`() = runTest {
        val remoteAlbumsDto = listOf(
            AlbumDto(
                1,
                1,
                "Title",
                "url",
                "thumb"
            )
        )
        val remoteAlbums = remoteAlbumsDto.map { it.toEntity() }

        coEvery { mockDao.getAll() } returns emptyList()
        coEvery { mockApi.getAlbums() } returns remoteAlbumsDto
        coEvery { mockDao.insertAll(remoteAlbums) } just Runs

        repository.getAlbums()

        // Verifica que só inseriu uma vez mesmo com lista vazia inicial
        coVerify(exactly = 1) { mockDao.insertAll(remoteAlbums) }
    }
}
