package com.raveline.leboncoinapplication.presentation.details

import androidx.lifecycle.SavedStateHandle
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.data.repository.AlbumRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: AlbumRepository = mockk()
    private lateinit var viewModel: AlbumDetailViewModel

    private val sampleAlbum = AlbumEntity(
        id = 1,
        albumId = 1,
        title = "Album 1",
        url = "url1",
        thumbnailUrl = "thumbnailUrl1"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `should set error if albumId is missing`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle()

        viewModel = AlbumDetailViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        assertEquals("Unknown error", viewModel.uiState.error)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.album)
    }

    @Test
    fun `should set isLoading true then false and populate album on success`() =
        runTest(testDispatcher) {
            val savedStateHandle = SavedStateHandle(mapOf("albumId" to "1"))
            coEvery { repository.getAlbumById(1) } coAnswers {
                delay(100)
                sampleAlbum
            }

            viewModel = AlbumDetailViewModel(repository, savedStateHandle)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.isLoading)
            assertEquals(sampleAlbum, viewModel.uiState.album)
            assertNull(viewModel.uiState.error)
        }

    @Test
    fun `should set error on exception from repository`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("albumId" to "1"))
        coEvery { repository.getAlbumById(1) } throws RuntimeException("Failed to fetch")

        viewModel = AlbumDetailViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
        assertEquals("Failed to fetch", viewModel.uiState.error)
        assertNull(viewModel.uiState.album)
    }

    @Test
    fun `should handle non-integer albumId safely`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("albumId" to "abc"))

        viewModel = AlbumDetailViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        assertEquals("Unknown error", viewModel.uiState.error)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.album)
    }

    @Test
    fun `should not call repository if albumId is null`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("albumId" to null))

        viewModel = AlbumDetailViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        assertNull(viewModel.uiState.album)
        assertEquals("Unknown error", viewModel.uiState.error)
    }

    @Test
    fun `isLoading should be true during loading and false after`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("albumId" to "1"))

        coEvery { repository.getAlbumById(1) } coAnswers {
            delay(100) // Simula um pequeno atraso na resposta
            sampleAlbum
        }

        viewModel = AlbumDetailViewModel(repository, savedStateHandle)

        // Avança o tempo só o suficiente para entrar no loading
        advanceTimeBy(200)
        assertTrue(viewModel.uiState.isLoading)

        // Agora finaliza a execução
        advanceUntilIdle()
        assertFalse(viewModel.uiState.isLoading)
        assertEquals(sampleAlbum, viewModel.uiState.album)
    }


    @Test
    fun `should not reload album if albumId is the same`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("albumId" to "1"))
        coEvery { repository.getAlbumById(1) } returns sampleAlbum

        viewModel = AlbumDetailViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        viewModel.uiState = viewModel.uiState.copy(isLoading = true)
        advanceUntilIdle()

        assertEquals(sampleAlbum, viewModel.uiState.album)
    }


}