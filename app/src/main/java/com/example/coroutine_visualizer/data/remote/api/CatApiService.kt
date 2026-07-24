package com.example.coroutine_visualizer.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET

interface CatApiService {
    @GET("cat")
    suspend fun getRandomCatImage(): ResponseBody
}