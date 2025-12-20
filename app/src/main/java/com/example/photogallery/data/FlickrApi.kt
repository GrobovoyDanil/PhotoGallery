package com.example.photogallery.data

import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {
    @GET("services/rest/?method=flickr.interestingness.getList&format=json&nojsoncallback=1&extras=url_s")
    suspend fun getPhotos(@Query("api_key") apiKey: String): FlickrResponse

    @GET("services/rest/?method=flickr.photos.search&format=json&nojsoncallback=1&extras=url_s")
    suspend fun searchPhotos(
        @Query("api_key") apiKey: String,
        @Query("text") text: String
    ): FlickrResponse
}
