package com.devesh.trivio_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api.php")
    fun getQuestions(
        @Query("amount") amount: Int,
        @Query("type") type: String,
        @Query("category") category: Int
    ): Call<QuestionResponse>
}
