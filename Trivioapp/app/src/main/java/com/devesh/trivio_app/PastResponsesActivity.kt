package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class PastResponsesActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var adapter: PastResponsesAdapter

    private lateinit var rvPastResponses: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_past_responses)

        rvPastResponses = findViewById(R.id.rv_past_responses)
        rvPastResponses.layoutManager = LinearLayoutManager(this)
        adapter = PastResponsesAdapter()
        rvPastResponses.adapter = adapter
        loadPastResponses()

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

    private fun loadPastResponses() {
        currentUser?.uid?.let { userId ->
            db.collection("quizResponses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    // Log all documents retrieved to verify the data
                    val responses = documents.toObjects(QuizResponse::class.java)
                    Log.d("QuizResponses", "Retrieved responses: $responses")  // Log all responses

                    // Submit the responses to the adapter
                    adapter.submitList(responses)

                    // Check total number of questions
                    val totalQuestions = responses.size
                    Log.d(
                        "QuizResponses",
                        "Total questions: $totalQuestions"
                    )  // Log total number of questions

                    // Count the correct answers
                    val correctAnswers = responses.count { it.isCorrect }
                    Log.d(
                        "QuizResponses",
                        "Correct answers: $correctAnswers"
                    )  // Log count of correct answers

                    // Update the progress bar if there are any questions
                    if (totalQuestions > 0) {
                        val progress = (correctAnswers * 100) / totalQuestions
                        Log.d(
                            "QuizResponses",
                            "Progress: $progress%"
                        )  // Log the progress percentage
                        updateProgressBar(progress)
                    } else {
                        Log.d(
                            "QuizResponses",
                            "No questions found."
                        )  // Log if no questions are found
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT)
                        .show()
                    Log.e(
                        "QuizResponses",
                        "Error getting documents: $exception"
                    )  // Log the error in case of failure
                }
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        Log.d("Firestore", "Document data: ${document.data}")
                    }
                    val responses = documents.toObjects(QuizResponse::class.java)
                    adapter.submitList(responses)

                }

        }
    }

    private fun updateProgressBar(progress: Int) {
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val progressPercentage = findViewById<TextView>(R.id.tv_progress_percentage)

        progressBar.progress = progress
        progressPercentage.text = "$progress%"
    }
}