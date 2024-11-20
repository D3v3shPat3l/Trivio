package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
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
                    val responses = documents.toObjects(QuizResponse::class.java)
                    adapter.submitList(responses)

                    val totalQuestions = responses.size
                    val correctAnswers = responses.count { it.isCorrect }

                    if (totalQuestions > 0) {
                        val progress = (correctAnswers * 100) / totalQuestions
                        updateProgressBar(progress)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
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
