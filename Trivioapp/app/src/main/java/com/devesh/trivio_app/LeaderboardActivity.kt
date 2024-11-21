package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Activity to display the leaderboard, showing top scores from Fire db
class LeaderboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private val db = FirebaseFirestore.getInstance()

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_leaderboard)

        recyclerView = findViewById(R.id.recycler_leaderboard)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchLeaderboard { leaderboard ->
            adapter = LeaderboardAdapter(leaderboard)
            recyclerView.adapter = adapter
        }

        // Set up the bottom navigation bar and its item selection actions
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

    // Fetches the top 10 leaderboard entries from Fire db, ordered by score in descending order
    private fun fetchLeaderboard(callback: (List<Map<String, Any>>) -> Unit) {
        db.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val leaderboard = documents.map { it.data }
                callback(leaderboard)
            }
    }
}
