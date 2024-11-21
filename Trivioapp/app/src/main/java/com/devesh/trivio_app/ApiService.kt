package com.devesh.trivio_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Interface for defining API endpoints to fetch trivia questions
interface ApiService {

    // Fetches a list of trivia questions based on the specified query parameters
    @GET("api.php")
    fun getQuestions(
        @Query("amount") amount: Int,
        @Query("type") type: String,
        @Query("category") category: Int
    ): Call<QuestionResponse>
}
