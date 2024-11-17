package com.devesh.trivio_app

data class QuizResponse(
    val userId: String = "",
    val quizId: String = "",
    val questionId: String = "",
    val answer: String = "",
    val questionText: String = "",
    val isCorrect: Boolean = false,
    val timestamp: Long = 0L,
    val score: Int = 0
)
