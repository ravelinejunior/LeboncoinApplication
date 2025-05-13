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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
        every { NetworkMonitor.observe(any()) } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should be default before loading`() = runTest(testDispatcher) {
        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        assertEquals(AlbumsUiState(), viewModel.uiState)
    }

    @Test
    fun `isLoading should be true at the start of load`() = runTest(testDispatcher) {
        val networkFlow = MutableSharedFlow<Boolean>()
        every { NetworkMonitor.observe(any()) } returns networkFlow

        coEvery { getAlbumsUseCase.invoke() } coAnswers {
            delay(100)
            emptyList()
        }

        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        networkFlow.emit(true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `should populate albums list when use case returns data`() = runTest(testDispatcher) {
        coEvery { getAlbumsUseCase.invoke() } returns sampleAlbums

        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        advanceUntilIdle()

        assertEquals(sampleAlbums, viewModel.uiState.albums)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.error)
    }

    @Test
    fun `should set error message when use case throws exception`() = runTest(testDispatcher) {
        val errorMessage = "Network error"
        coEvery { getAlbumsUseCase.invoke() } throws RuntimeException(errorMessage)

        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.uiState.error)
        assertTrue(viewModel.uiState.albums.isEmpty())
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `should reload albums when network becomes available`() = runTest(testDispatcher) {
        val networkFlow = MutableSharedFlow<Boolean>()
        every { NetworkMonitor.observe(any()) } returns networkFlow

        coEvery { getAlbumsUseCase.invoke() } returns emptyList() andThen sampleAlbums

        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        networkFlow.emit(false)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.albums.isEmpty())

        networkFlow.emit(true)
        advanceUntilIdle()

        assertEquals(sampleAlbums, viewModel.uiState.albums)
    }

    @Test
    fun `should reload albums only once even after network becomes available when albums already exist`() = runTest(testDispatcher) {
        val networkFlow = MutableSharedFlow<Boolean>()
        every { NetworkMonitor.observe(any()) } returns networkFlow

        coEvery { getAlbumsUseCase.invoke() } returns sampleAlbums

        viewModel = AlbumsViewModel(getAlbumsUseCase, savedStateHandle, application)
        advanceUntilIdle()

        assertEquals(sampleAlbums, viewModel.uiState.albums)

        networkFlow.emit(false)
        networkFlow.emit(true)
        advanceUntilIdle()

        assertEquals(sampleAlbums, viewModel.uiState.albums)
        coVerify(exactly = 2) { getAlbumsUseCase.invoke() }
    }
}
