package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Html
import android.os.Build

// This activity handles the True/False quiz flow, including fetching questions, scoring, and saving responses
class TFQuizActivity : AppCompatActivity() {

    private var currentQuestionIndex = 0
    private var totalQuestions = 0
    private var score = 0
    private lateinit var questionList: List<Question>
    private lateinit var quizId: String
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_torf)

        quizId = System.currentTimeMillis().toString()
        totalQuestions = intent.getIntExtra("NUMBER_OF_QUESTIONS", 0)
        val categoryId = intent.getIntExtra("CATEGORY_ID", 9)
        val questionText = findViewById<TextView>(R.id.tv_question_tf)
        val questionNumberText = findViewById<TextView>(R.id.tv_question_number)
        val trueButton = findViewById<Button>(R.id.btn_true)
        val falseButton = findViewById<Button>(R.id.btn_false)

        trueButton.isEnabled = false
        falseButton.isEnabled = false

        fetchQuestionsFromApi(categoryId, trueButton, falseButton)

        trueButton.setOnClickListener {
            checkAnswer("True", questionText, questionNumberText)
        }

        falseButton.setOnClickListener {
            checkAnswer("False", questionText, questionNumberText)
        }

        // Setup the bottom navigation menu
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.action_quiz -> {
                    startActivity(Intent(this, QuizMeOptionsActivity::class.java))
                    true
                }
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // Method to fetch questions from the API using Retrofit
    private fun fetchQuestionsFromApi(categoryId: Int, trueButton: Button, falseButton: Button) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        val quizType = intent.getStringExtra("QUIZ_TYPE") ?: "boolean"

        val call = apiService.getQuestions(
            amount = totalQuestions,
            type = quizType,
            category = categoryId
        )

        // Handle the response of the API call
        call.enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(call: Call<QuestionResponse>, response: Response<QuestionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    questionList = response.body()!!.results
                    totalQuestions = questionList.size

                    if (totalQuestions > 0) {
                        trueButton.isEnabled = true
                        falseButton.isEnabled = true
                        updateQuestionUI()
                    } else {
                        Toast.makeText(this@TFQuizActivity, "No questions available", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@TFQuizActivity, "Failed to fetch questions", Toast.LENGTH_SHORT).show()
                }
            }

            // Handle failure in API call
            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                Toast.makeText(this@TFQuizActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Extension function to decode HTML entities in the question text
    fun String.decodeHtml(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(this).toString()
        }
    }

    // Update the UI with the current question and question number
    private fun updateQuestionUI() {
        val questionText = findViewById<TextView>(R.id.tv_question_tf)
        val questionNumberText = findViewById<TextView>(R.id.tv_question_number)

        if (currentQuestionIndex < totalQuestions) {
            questionNumberText.text = "${currentQuestionIndex + 1}/$totalQuestions"
            questionText.text = questionList[currentQuestionIndex].question.decodeHtml()
        }
    }

    // Check the selected answer and update score
    private fun checkAnswer(selectedAnswer: String, questionText: TextView, questionNumberText: TextView) {
        if (questionList.isNotEmpty() && currentQuestionIndex < questionList.size) {
            val currentQuestion = questionList[currentQuestionIndex]
            val correctAnswer = currentQuestion.correct_answer
            val isCorrect = selectedAnswer == correctAnswer

            if (isCorrect) {
                score++
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Incorrect. The correct answer was: $correctAnswer", Toast.LENGTH_SHORT).show()
            }

            saveQuizResponse(
                quizId = quizId,
                questionId = currentQuestionIndex.toString(),
                answer = selectedAnswer,
                isCorrect = isCorrect,
                score = score
            )

            showNextQuestion(questionText, questionNumberText)
        }
    }

    // Move to the next question or show quiz summary if all questions are answered
    private fun showNextQuestion(questionText: TextView, questionNumberText: TextView) {
        currentQuestionIndex++
        if (currentQuestionIndex < totalQuestions) {
            updateQuestionUI()
        } else {
            showQuizSummary()
        }
    }

    // Show the final quiz summary with score and options to replay or exit
    private fun showQuizSummary() {
        val summaryDialog = QuizSummaryDialog(
            score = score,
            totalQuestions = totalQuestions,
            onPlayAgain = { restartQuiz() },
            onGoToMenu = { finish() }
        )
        summaryDialog.show(supportFragmentManager, "QuizSummaryDialog")
    }

    // Restart the quiz by resetting score and question index
    private fun restartQuiz() {
        score = 0
        currentQuestionIndex = 0

        val trueButton = findViewById<Button>(R.id.btn_true)
        val falseButton = findViewById<Button>(R.id.btn_false)

        trueButton.isEnabled = false
        falseButton.isEnabled = false

        fetchQuestionsFromApi(intent.getIntExtra("CATEGORY_ID", 9), trueButton, falseButton)
    }

    // Save the user's response to Fire db
    private fun saveQuizResponse(quizId: String, questionId: String, answer: String, isCorrect: Boolean, score: Int) {
        val questionText = questionList[currentQuestionIndex].question.decodeHtml()
        val username = currentUser?.email?.substringBefore("@") ?: "Anonymous"
        val response: Map<String, Any> = hashMapOf(
            "userId" to (currentUser?.uid ?: ""),
            "username" to username,
            "quizId" to quizId,
            "questionId" to questionId,
            "questionText" to questionText,
            "answer" to answer,
            "isCorrect" to isCorrect,
            "timestamp" to System.currentTimeMillis(),
            "score" to score
        )

        db.collection("quizResponses").add(response)
        updateLeaderboard(score, username)
    }

    // Update the user's score in the leaderboard
    private fun updateLeaderboard(score: Int, username: String) {
        val userRef = db.collection("leaderboard").document(currentUser?.uid ?: return)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentScore = document.getLong("score")?.toInt() ?: 0
                val newScore = currentScore + score
                userRef.update("score", newScore, "username", username)
            } else {
                userRef.set(hashMapOf("score" to score, "username" to username))
            }
        }
    }
}
