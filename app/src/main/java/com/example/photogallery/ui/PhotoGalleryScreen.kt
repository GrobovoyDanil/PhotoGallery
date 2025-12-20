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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

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

    fun removeFromFavorites(photo: PhotoItem) {
        viewModelScope.launch {
            dao.deleteById(photo.id)
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
                if (query.isBlank()) {
                    loadPhotos() // возвращаем дефолтный список
                    return@launch
                }
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

    suspend fun toggleFavorite(photo: PhotoItem): List<FavoritePhoto> {
        val currentFavorites = dao.getAll().toMutableList()
        val exists = currentFavorites.any { it.id == photo.id }
        if (exists) {
            dao.deleteById(photo.id)
            currentFavorites.removeAll { it.id == photo.id }
        } else {
            dao.insert(FavoritePhoto(photo.id, photo.title, photo.imageUrl ?: ""))
            currentFavorites.add(FavoritePhoto(photo.id, photo.title, photo.imageUrl ?: ""))
        }
        return currentFavorites
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
    var favorites by remember { mutableStateOf<List<FavoritePhoto>>(emptyList()) }

    var showPhotoDialog by remember { mutableStateOf<PhotoItem?>(null) }

    showPhotoDialog?.let { photo ->
        AlertDialog(
            onDismissRequest = { showPhotoDialog = null },
            title = { Text(photo.title) },
            text = {
                Column {
                    AsyncImage(
                        model = photo.imageUrl,
                        contentDescription = photo.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoDialog = null }) {
                    Text("Закрыть")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        favorites = viewModel.getFavorites()
    }

    Scaffold(
        topBar = {
            PhotoGalleryTopBar(
                onSearchClick = { showSearchDialog = true },
                onFavoritesClick = { showFavorites = !showFavorites },
                onClearClick = {
                    viewModel.clearFavorites()
                    favorites = emptyList()
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

        val displayPhotos = if (showFavorites) {
            favorites.map { PhotoItem(it.id, it.title, it.imageUrl) }
        } else photos

//        PhotoList(
//            photos = displayPhotos,
//            favorites = favorites,
//            modifier = Modifier.padding(paddingValues),
//            onFavoriteClick = { photo ->
//                viewModel.addToFavorites(photo)
//                if (favorites.none { it.id == photo.id }) {
//                    favorites = favorites + FavoritePhoto(photo.id, photo.title, photo.imageUrl ?: "")
//                }
//            },
//            onRemoveFavorite = { photo ->
//                viewModel.removeFromFavorites(photo)
//                favorites = favorites.filter { it.id != photo.id }
//            },
//            onPhotoClick = { photo ->
//                showPhotoDialog = photo
//            }
//        )

        val scope = rememberCoroutineScope()

        PhotoList(
            photos = displayPhotos,
            favorites = favorites,
            modifier = Modifier.padding(paddingValues),
            onPhotoClick = { photo -> showPhotoDialog = photo },
            onFavoriteToggle = { photo ->
                scope.launch {
                    val updatedFavorites = viewModel.toggleFavorite(photo)
                    favorites = updatedFavorites
                }
            }
        )
    }
}

@Composable
fun PhotoList(
    photos: List<PhotoItem>,
    favorites: List<FavoritePhoto>,
    modifier: Modifier = Modifier,
//    onFavoriteClick: (PhotoItem) -> Unit,
//    onRemoveFavorite: (PhotoItem) -> Unit,
    onPhotoClick: (PhotoItem) -> Unit,
    onFavoriteToggle: (PhotoItem) -> Unit,
) {
    // val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(photos) { photo ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
//                    .clickable {
//                        if (favorites.any { it.id == photo.id }) {
//                            onRemoveFavorite(photo)
//                        } else {
//                            onFavoriteClick(photo)
//                        }
//                    }
                    .pointerInput(photo.id) {
                        detectTapGestures(
                            onTap = { onPhotoClick(photo) },
                            onDoubleTap = { onFavoriteToggle(photo) }
                        )
                    }
            ) {
                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = photo.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(30.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = if (favorites.any { it.id == photo.id }) Color.Red else Color.Gray,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(20.dp)
                    )
                }
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

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        if (favorites.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Избранного нет", color = Color.White)
                }
            }
        } else {
            items(favorites) { photo ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                ) {
                    AsyncImage(
                        model = photo.imageUrl,
                        contentDescription = photo.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.Red,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                    )
                }
            }
        }
    }
}



