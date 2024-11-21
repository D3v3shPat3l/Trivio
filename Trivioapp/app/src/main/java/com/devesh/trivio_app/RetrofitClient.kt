package com.devesh.trivio_app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton object to provide a Retrofit instance for making API requests.
object RetrofitClient {
    private const val BASE_URL = "https://opentdb.com/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

