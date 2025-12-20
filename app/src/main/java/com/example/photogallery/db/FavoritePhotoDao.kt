package com.example.photogallery.db

import androidx.room.*


@Dao
interface FavoritePhotoDao {
    @Query("SELECT * FROM favorite_photos")
    suspend fun getAll(): List<FavoritePhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: FavoritePhoto)

    @Query("DELETE FROM favorite_photos")
    suspend fun deleteAll()

    @Query("DELETE FROM favorite_photos WHERE id = :id")
    suspend fun deleteById(id: String)
}
