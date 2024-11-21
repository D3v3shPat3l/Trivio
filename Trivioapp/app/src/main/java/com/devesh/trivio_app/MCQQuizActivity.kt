package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Html
import android.os.Build

// This activity handles the Multiple Choice Quiz flow, fetching questions, managing answers, and saving results
class MCQQuizActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_mcq)

        quizId = System.currentTimeMillis().toString()
        totalQuestions = intent.getIntExtra("NUMBER_OF_QUESTIONS", 0)
        val categoryId = intent.getIntExtra("CATEGORY_ID", 9)
        val option1Button = findViewById<Button>(R.id.btn_option_1)
        val option2Button = findViewById<Button>(R.id.btn_option_2)
        val option3Button = findViewById<Button>(R.id.btn_option_3)
        val option4Button = findViewById<Button>(R.id.btn_option_4)

        listOf(option1Button, option2Button, option3Button, option4Button).forEach {
            it.isEnabled = false
        }

        fetchQuestionsFromApi(categoryId, option1Button, option2Button, option3Button, option4Button)

        option1Button.setOnClickListener { checkAnswer(option1Button.text.toString()) }
        option2Button.setOnClickListener { checkAnswer(option2Button.text.toString()) }
        option3Button.setOnClickListener { checkAnswer(option3Button.text.toString()) }
        option4Button.setOnClickListener { checkAnswer(option4Button.text.toString()) }

        // Bottom navigation for accessing different parts of the app
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

    // Fetches questions from the API based on the selected category and quiz type
    private fun fetchQuestionsFromApi(
        categoryId: Int,
        option1Button: Button,
        option2Button: Button,
        option3Button: Button,
        option4Button: Button
    ) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val quizType = "multiple"

        val call = apiService.getQuestions(
            amount = totalQuestions,
            type = quizType,
            category = categoryId
        )

        // Make an API call to fetch questions
        call.enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(call: Call<QuestionResponse>, response: Response<QuestionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    questionList = response.body()!!.results
                    totalQuestions = questionList.size

                    if (totalQuestions > 0) {
                        listOf(option1Button, option2Button, option3Button, option4Button).forEach {
                            it.isEnabled = true
                        }
                        updateQuestionUI()
                    } else {
                        Toast.makeText(this@MCQQuizActivity, "No questions available", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@MCQQuizActivity, "Failed to fetch questions", Toast.LENGTH_SHORT).show()
                }
            }

            // Handle failure in API call
            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                Toast.makeText(this@MCQQuizActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Helper function to decode HTML entities in the question and options
    fun String.decodeHtml(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(this).toString()
        }
    }

    // Updates the UI with the current question and answer options
    private fun updateQuestionUI() {
        val questionText = findViewById<TextView>(R.id.tv_question_mcq)
        val questionNumberText = findViewById<TextView>(R.id.tv_question_number)
        val option1Button = findViewById<Button>(R.id.btn_option_1)
        val option2Button = findViewById<Button>(R.id.btn_option_2)
        val option3Button = findViewById<Button>(R.id.btn_option_3)
        val option4Button = findViewById<Button>(R.id.btn_option_4)

        if (currentQuestionIndex < totalQuestions) {
            questionNumberText.text = "${currentQuestionIndex + 1}/$totalQuestions"
            val currentQuestion = questionList[currentQuestionIndex]
            questionText.text = currentQuestion.question.decodeHtml()

            val options = currentQuestion.incorrect_answers.toMutableList()
            options.add(currentQuestion.correct_answer)
            options.shuffle()

            option1Button.text = options[0].decodeHtml()
            option2Button.text = options[1].decodeHtml()
            option3Button.text = options[2].decodeHtml()
            option4Button.text = options[3].decodeHtml()
        }
    }

    // Checks if the selected answer is correct and updates the score
    private fun checkAnswer(selectedAnswer: String) {
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
            isCorrect = isCorrect
        )

        showNextQuestion()
    }

    // Moves to the next question or shows the quiz summary if all questions are answered
    private fun showNextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex < totalQuestions) {
            updateQuestionUI()
        } else {
            showQuizSummary()
        }
    }

    // Displays a dialog showing the quiz results and offers options to play again or exit
    private fun showQuizSummary() {
        val summaryDialog = QuizSummaryDialog(
            score = score,
            totalQuestions = totalQuestions,
            onPlayAgain = { restartQuiz() },
            onGoToMenu = { finish() }
        )
        summaryDialog.show(supportFragmentManager, "QuizSummaryDialog")
    }

    // Restarts the quiz by resetting score and question index, and fetching new questions
    private fun restartQuiz() {
        score = 0
        currentQuestionIndex = 0
        fetchQuestionsFromApi(intent.getIntExtra("CATEGORY_ID", 9),
            findViewById(R.id.btn_option_1),
            findViewById(R.id.btn_option_2),
            findViewById(R.id.btn_option_3),
            findViewById(R.id.btn_option_4)
        )
    }

    // Saves the user's response to Fire db along with the score and other details
    private fun saveQuizResponse(quizId: String, questionId: String, answer: String, isCorrect: Boolean) {
        val questionText = questionList[currentQuestionIndex].question.decodeHtml()
        val username = currentUser?.email?.substringBefore("@") ?: "Anonymous"

        val response = hashMapOf(
            "userId" to currentUser?.uid,
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

    // Updates the leaderboard with the user's new score
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
