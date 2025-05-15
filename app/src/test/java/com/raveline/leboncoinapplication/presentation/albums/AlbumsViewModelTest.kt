package com.raveline.leboncoinapplication.presentation.albums

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.SavedStateHandle
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.domain.use_case.GetAlbumsUseCase
import com.raveline.leboncoinapplication.utils.NetworkMonitor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val getAlbumsUseCase: GetAlbumsUseCase = mockk()
    private val application: Application = mockk()
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
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
        val connectivityManager: ConnectivityManager = mockk(relaxed = true)
        every { application.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { application.applicationContext } returns application
        mockkObject(NetworkMonitor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should be default before loading`() = runTest(testDispatcher) {
        every { NetworkMonitor.observe(any()) } returns flowOf(true)
        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        assertEquals(false, viewModel.uiState.isLoading)
        assertTrue(viewModel.uiState.albums.isEmpty())
        assertNull(viewModel.uiState.error)
    }

    @Test
    fun `refreshAlbums should load data successfully`() = runTest(testDispatcher) {
        every { NetworkMonitor.observe(any()) } returns flowOf(true)
        coEvery { getAlbumsUseCase.invoke(any()) } returns sampleAlbums

        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
        assertEquals(sampleAlbums, viewModel.uiState.albums)
        assertNull(viewModel.uiState.error)
    }

    @Test
    fun `refreshAlbums should set error when useCase throws`() = runTest(testDispatcher) {
        every { NetworkMonitor.observe(any()) } returns flowOf(true)
        val errorMsg = "Network Error"
        coEvery { getAlbumsUseCase.invoke(any()) } throws RuntimeException(errorMsg)

        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
        assertTrue(viewModel.uiState.albums.isEmpty())
        assertEquals(errorMsg, viewModel.uiState.error)
    }

    @Test
    fun `observeNetwork should force refresh when connected`() = runTest(testDispatcher) {
        val networkFlow = MutableSharedFlow<Boolean>()
        every { NetworkMonitor.observe(any()) } returns networkFlow
        coEvery { getAlbumsUseCase.invoke(true) } returns sampleAlbums

        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        networkFlow.emit(false)
        advanceTimeBy(1000)
        networkFlow.emit(true)
        advanceUntilIdle()

        coVerify { getAlbumsUseCase.invoke(true) }
        assertEquals(sampleAlbums, viewModel.uiState.albums)
    }

    @Test
    fun `toggleLayout should flip grid state and persist`() = runTest(testDispatcher) {
        every { NetworkMonitor.observe(any()) } returns flowOf(true)
        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        assertFalse(viewModel.isGrid)

        viewModel.toggleLayout()
        assertTrue(viewModel.isGrid)
        assertEquals(true, savedStateHandle.get<Boolean>("is_grid_layout"))
    }
}
