package com.example.photogallery.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryTopBar(
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onClearClick: () -> Unit
) {
    TopAppBar(
        title = { Text("PhotoGallery") },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = onFavoritesClick) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorites")
            }
            IconButton(onClick = onClearClick) {
                Icon(Icons.Default.Delete, contentDescription = "Clear DB")
            }
        }
    )
}