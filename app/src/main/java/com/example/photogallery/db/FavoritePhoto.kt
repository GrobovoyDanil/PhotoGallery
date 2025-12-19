package com.example.photogallery.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_photos")
data class FavoritePhoto(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String
)
