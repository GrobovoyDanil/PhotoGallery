package com.example.photogallery.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.photogallery.data.PhotoItem
import com.example.photogallery.data.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import com.example.photogallery.db.DatabaseProvider
import com.example.photogallery.db.FavoritePhoto
import com.example.photogallery.db.FavoritePhotoDao
import androidx.compose.material3.AlertDialog

class PhotoGalleryViewModel : ViewModel() {
    lateinit var dao: FavoritePhotoDao

    fun initDatabase(context: Context) {
        dao = DatabaseProvider.get(context).favoritePhotoDao()
    }

    fun addToFavorites(photo: PhotoItem) {
        if (photo.imageUrl == null) return

        viewModelScope.launch {
            dao.insert(
                FavoritePhoto(
                    id = photo.id,
                    title = photo.title,
                    imageUrl = photo.imageUrl
                )
            )
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }

    suspend fun getFavorites(): List<FavoritePhoto> {
        return dao.getAll()
    }

    private val _photos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val photos: StateFlow<List<PhotoItem>> = _photos


    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            try {
                val response = PhotoRepository.api.getPhotos(
                    apiKey = "c19cc8f4173598aa3908927fd6adbe88"
                )

                if (response.stat == "ok") {
                    _photos.value = response.photos.photo
                } else {
                    android.util.Log.e("FLICKR", "API error: ${response.stat}")
                }

            } catch (e: Exception) {
                android.util.Log.e("FLICKR", "Parse/network error", e)
            }
        }
    }

    fun searchPhotos(query: String) {
        viewModelScope.launch {
            try {
                val response = PhotoRepository.api.searchPhotos(
                    apiKey = "c19cc8f4173598aa3908927fd6adbe88",
                    text = query
                )

                if (response.stat == "ok") {
                    _photos.value = response.photos.photo
                } else {
                    android.util.Log.e("FLICKR", "Search API error")
                }
            } catch (e: Exception) {
                android.util.Log.e("FLICKR", "Search error", e)
            }
        }
    }
}

@Composable
fun PhotoGalleryScreen(
    viewModel: PhotoGalleryViewModel = viewModel()
) {
    val photos by viewModel.photos.collectAsState()
    var showFavorites by remember { mutableStateOf(false) }

    var showSearchDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            PhotoGalleryTopBar(
                onSearchClick = {
                    showSearchDialog = true
                },
                onFavoritesClick = {
                    showFavorites = !showFavorites
                },
                onClearClick = {
                    viewModel.clearFavorites()
                }
            )
        }
    ) { paddingValues ->

        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                title = { Text("Поиск фото") },
                text = {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Ключевое слово") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.searchPhotos(searchText)
                        showSearchDialog = false
                    }) {
                        Text("Искать")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSearchDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
        if (showFavorites) {
            FavoritesScreen(
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel
            )
        } else {
            PhotoList(
                photos = photos,
                modifier = Modifier.padding(paddingValues),
                onPhotoClick = { viewModel.addToFavorites(it) }
            )
        }
    }
}

@Composable
fun PhotoList(
    photos: List<PhotoItem>,
    modifier: Modifier = Modifier,
    onPhotoClick: (PhotoItem) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(8.dp)
    ) {
        if (photos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка...", color = Color.White)
                }
            }
        } else {
            items(photos) { photo ->
                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = photo.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 6.dp)
                        .clickable {
                            onPhotoClick(photo)
                        }
                )
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotoGalleryViewModel
) {
    var favorites by remember { mutableStateOf<List<FavoritePhoto>>(emptyList()) }

    LaunchedEffect(Unit) {
        favorites = viewModel.getFavorites()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(8.dp)
    ) {
        if (favorites.isEmpty()) {
            item {
                Text("Избранного нет", color = Color.White)
            }
        } else {
            items(favorites) { photo ->
                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = photo.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 6.dp)
                )
            }
        }
    }
}



