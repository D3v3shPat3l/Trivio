package com.devesh.trivio_app

// Data class representing the response from the API with a response code
data class QuestionResponse(
    val response_code: Int,
    val results: List<Question>
)

// Data class representing a single quiz question along with its answers
data class Question(
    val category: String,
    val type: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)
