package com.raveline.leboncoinapplication.presentation.albums

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.raveline.leboncoinapplication.R
import com.raveline.leboncoinapplication.data.local.entity.AlbumEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AlbumsScreen(
    albums: List<AlbumEntity>,
    onAlbumClick: (Int) -> Unit,
    viewModel: AlbumsViewModel
) {
    val isGrid = viewModel.isGrid

    val rotationAngle by animateFloatAsState(
        targetValue = if (isGrid) 180f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    val scale by animateFloatAsState(
        targetValue = if (isGrid) 1.2f else 1f,
        animationSpec = tween(durationMillis = 600)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Albums",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                    )
                },
                actions = {

                    IconButton(
                        onClick = { viewModel.toggleLayout() },
                        modifier = Modifier
                            .size(48.dp)
                            .rotate(rotationAngle)
                            .scale(scale)
                    ) {
                        Icon(
                            imageVector = if (isGrid) Icons.AutoMirrored.Filled.List
                            else Icons.Rounded.MoreVert,
                            contentDescription = "Change Layout"
                        )
                    }
                }
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = isGrid,
            transitionSpec = {
                fadeIn(tween(600)).togetherWith(fadeOut(tween(600)))
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { targetIsGrid ->
            if (targetIsGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(albums) { album ->
                        AlbumItem(album = album, isGrid = true) { onAlbumClick(album.id) }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(albums) { album ->
                        AlbumItem(album = album, isGrid = false) { onAlbumClick(album.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumItem(album: AlbumEntity, isGrid: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(album.thumbnailUrl)
            .httpHeaders(
                NetworkHeaders.Builder()
                    .add("User-Agent", "LeboncoinApp/1.0")
                    .build()
            )
            .crossfade(true)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error_image)
            .build()
    )
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (isGrid)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Album #${album.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        album.title, style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Album #${album.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
        }
    }
}
