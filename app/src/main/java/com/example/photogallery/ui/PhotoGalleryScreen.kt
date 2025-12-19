package com.example.photogallery.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

class PhotoGalleryViewModel : ViewModel() {

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
}

@Composable
fun PhotoGalleryScreen(
    viewModel: PhotoGalleryViewModel = viewModel()
) {
    val photos by viewModel.photos.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(8.dp)
    ) {
        if (photos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize() // Занимает всю доступную высоту LazyColumn
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Загрузка...",
                        color = Color.White,
                        fontSize = 18.sp
                    )
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
                )
            }
        }
    }
}