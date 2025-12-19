package com.example.photogallery.data

import com.squareup.moshi.Json

data class FlickrResponse(
    val photos: Photos
)

data class Photos(
    val photo: List<PhotoItem>
)

data class PhotoItem(
    val id: String,
    val title: String,
    @Json(name = "url_s") val imageUrl: String?
)
