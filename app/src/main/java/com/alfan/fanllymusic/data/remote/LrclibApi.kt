package com.alfan.fanllymusic.data.remote

import com.alfan.fanllymusic.data.remote.model.LrclibResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LrclibApi {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("artist_name") artistName: String,
        @Query("track_name") trackName: String,
        @Query("album_name") albumName: String,
        @Query("duration") duration: Long
    ): LrclibResponse
}
