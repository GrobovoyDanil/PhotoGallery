package com.example.photogallery.data

import com.squareup.moshi.Json

data class FlickrResponse(
    val photos: Photos,
    val stat: String
)

data class Photos(
    val page: Int,
    val pages: Int,
    val perpage: Int,
    val total: Int,
    val photo: List<PhotoItem>
)

data class PhotoItem(
    val id: String,
    val title: String,
    @Json(name = "url_s") val imageUrl: String?
)
