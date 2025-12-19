package com.example.photogallery.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [FavoritePhoto::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritePhotoDao(): FavoritePhotoDao
}
