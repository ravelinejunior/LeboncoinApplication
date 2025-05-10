package com.raveline.leboncoinapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.raveline.leboncoinapplication.presentation.albums.AlbumsScreen
import com.raveline.leboncoinapplication.presentation.details.AlbumDetailScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "albums") {
        composable("albums") {
            AlbumsScreen(onAlbumClick = { id ->
                navController.navigate("albumDetail/$id")
            })
        }
        composable("albumDetail/{albumId}") { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")?.toIntOrNull()
            albumId?.let {
                AlbumDetailScreen(
                    albumId = it,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}