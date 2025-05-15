package com.raveline.leboncoinapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.raveline.leboncoinapplication.presentation.albums.AlbumsScreen
import com.raveline.leboncoinapplication.presentation.albums.AlbumsViewModel
import com.raveline.leboncoinapplication.presentation.details.AlbumDetailScreen
import com.raveline.leboncoinapplication.presentation.details.AlbumDetailViewModel
import com.raveline.leboncoinapplication.presentation.splash.SplashScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen {
                navController.navigate(Screen.Albums.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Albums.route) {
            val viewModel = hiltViewModel<AlbumsViewModel>()
            val state = viewModel.uiState
            AlbumsScreen(
                uiState = state,
                isGrid = viewModel.isGrid,
                onToggleLayout = viewModel::toggleLayout,
                onAlbumClick = { id ->
                    navController.navigate(Screen.AlbumDetail.createRoute(id))
                },
            )
        }
        composable(Screen.AlbumDetail.route) { backStackEntry ->
            val viewModel = hiltViewModel<AlbumDetailViewModel>()
            val state = viewModel.uiState
            val albumId = backStackEntry.arguments?.getString("albumId")?.toIntOrNull()
            albumId?.let {
                AlbumDetailScreen(
                    state = state,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Albums : Screen("albums")
    object AlbumDetail : Screen("albumDetail/{albumId}") {
        fun createRoute(albumId: Int) = "albumDetail/$albumId"
    }
}