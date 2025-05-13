package com.raveline.leboncoinapplication.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.raveline.leboncoinapplication.R
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity
import com.raveline.leboncoinapplication.ui.theme.LeboncoinApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    state: AlbumDetailUiState,
    onBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        }

        state.album != null -> {
            val album = state.album
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                album.title, style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Top
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.url)
                            .httpHeaders(
                                NetworkHeaders.Builder()
                                    .add("User-Agent", "LeboncoinApp/1.0")
                                    .build()
                            )
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        error = painterResource(id = R.drawable.error_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Title: ${album.title}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ID: ${album.id}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlbumDetailScreenPreview() {
    LeboncoinApplicationTheme {
        AlbumDetailScreen(
            state = AlbumDetailUiState(
                album = AlbumEntity(
                    id = 1,
                    albumId = 1,
                    title = "Album 1",
                    thumbnailUrl = "https://via.placeholder.com/150",
                    url = "https://via.placeholder.com/150",
                )
            )
        )
    }
}