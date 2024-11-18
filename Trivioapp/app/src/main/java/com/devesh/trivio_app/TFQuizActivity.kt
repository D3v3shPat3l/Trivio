package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class TFQuizActivity : AppCompatActivity() {

    private var currentQuestionIndex = 0
    private var totalQuestions = 0
    private var score = 0
    private lateinit var questionList: List<Question>
    private lateinit var quizId: String
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun fetchQuestionsFromApi(categoryId: Int, trueButton: Button, falseButton: Button) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        val quizType = intent.getStringExtra("QUIZ_TYPE") ?: "boolean"

        val call = apiService.getQuestions(
            amount = totalQuestions,
            type = quizType,
            category = categoryId
        )

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

            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                Toast.makeText(this@TFQuizActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateQuestionUI() {
        val questionText = findViewById<TextView>(R.id.tv_question_tf)
        val questionNumberText = findViewById<TextView>(R.id.tv_question_number)

        if (currentQuestionIndex < totalQuestions) {
            questionNumberText.text = "${currentQuestionIndex + 1}/$totalQuestions"
            questionText.text = questionList[currentQuestionIndex].question
        }
    }

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

    private fun showNextQuestion(questionText: TextView, questionNumberText: TextView) {
        currentQuestionIndex++
        if (currentQuestionIndex < totalQuestions) {
            updateQuestionUI()
        } else {
            showQuizSummary()
        }
    }

    private fun showQuizSummary() {
        val summaryDialog = QuizSummaryDialog(
            score = score,
            totalQuestions = totalQuestions,
            onPlayAgain = { restartQuiz() },
            onGoToMenu = { finish() }
        )
        summaryDialog.show(supportFragmentManager, "QuizSummaryDialog")
    }

    private fun restartQuiz() {
        score = 0
        currentQuestionIndex = 0

        val trueButton = findViewById<Button>(R.id.btn_true)
        val falseButton = findViewById<Button>(R.id.btn_false)

        trueButton.isEnabled = false
        falseButton.isEnabled = false

        fetchQuestionsFromApi(intent.getIntExtra("CATEGORY_ID", 9), trueButton, falseButton)
    }

    private fun saveQuizResponse(quizId: String, questionId: String, answer: String, isCorrect: Boolean, score: Int) {
        val questionText = questionList[currentQuestionIndex].question
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
            .addOnSuccessListener {
                Log.d("Firestore", "Quiz response saved successfully!")
                updateLeaderboard(score, username)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error saving quiz response", e)
            }
    }

    private fun updateLeaderboard(score: Int, username: String) {
        val userRef = db.collection("leaderboard").document(currentUser?.uid ?: return)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentScore = document.getLong("score")?.toInt() ?: 0
                val newScore = currentScore + score
                userRef.update("score", newScore, "username", username)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Leaderboard updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating leaderboard", e)
                    }
            } else {
                userRef.set(hashMapOf("score" to score, "username" to username))
                    .addOnSuccessListener {
                        Log.d("Firestore", "New leaderboard entry created")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error adding leaderboard entry", e)
                    }
            }
        }
    }
}
