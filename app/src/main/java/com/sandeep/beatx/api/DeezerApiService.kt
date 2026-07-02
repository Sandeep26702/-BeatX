package com.sandeep.beatx.api

import com.sandeep.beatx.model.SongResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerApiService {
    @GET("chart/0/tracks")
    suspend fun getTopTracks(@Query("limit") limit: Int = 30): SongResponse

    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): SongResponse
}
