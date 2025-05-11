import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.domain.use_case.GetAlbumsUseCase
import com.raveline.leboncoinapplication.presentation.albums.AlbumsUiState
import com.raveline.leboncoinapplication.presentation.albums.AlbumsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getAlbumsUseCase: GetAlbumsUseCase = mockk()
    private lateinit var viewModel: AlbumsViewModel

    private val sampleAlbum = AlbumEntity(
        id = 1,
        albumId = 1,
        title = "Album 1",
        url = "url1",
        thumbnailUrl = "thumbnailUrl1"
    )

    private val sampleAlbums = listOf(sampleAlbum)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default before loading`() {
        viewModel = AlbumsViewModel(getAlbumsUseCase)

        val expectedState = AlbumsUiState()
        assertEquals(expectedState, viewModel.uiState)
    }

    @Test
    fun `isLoading should be true at the start of load`() = runTest {
        coEvery { getAlbumsUseCase.invoke() } coAnswers {
            delay(100)
            emptyList()
        }

        viewModel = AlbumsViewModel(getAlbumsUseCase)

        runCurrent()

        assertTrue(viewModel.uiState.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
    }


    @Test
    fun `should populate albums list and set isLoading false when use case returns data`() = runTest {
        coEvery { getAlbumsUseCase.invoke() } returns sampleAlbums

        viewModel = AlbumsViewModel(getAlbumsUseCase)

        advanceUntilIdle()

        assertEquals(sampleAlbums, viewModel.uiState.albums)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.error)
    }

    @Test
    fun `should set error message and clear albums when use case throws exception`() = runTest {
        val errorMessage = "Network error"
        coEvery { getAlbumsUseCase.invoke() } throws RuntimeException(errorMessage)

        viewModel = AlbumsViewModel(getAlbumsUseCase)

        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.uiState.error)
        assertTrue(viewModel.uiState.albums.isEmpty())
        assertFalse(viewModel.uiState.isLoading)
    }
}
