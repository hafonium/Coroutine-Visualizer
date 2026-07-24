package com.example.coroutine_visualizer.data.remote.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.thecodecup.data.remote.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatRepository {
    private val apiService = ApiClient.catApi

    suspend fun getRandomCatImage(): Bitmap? {
        // Perform the network request on the IO dispatcher to avoid blocking the main thread
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRandomCatImage()
                BitmapFactory.decodeStream(response.byteStream())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}