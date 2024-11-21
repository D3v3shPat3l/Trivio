package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Activity to display the user's achievements in a RecyclerView
class AchievementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_achievements)

        recyclerView = findViewById(R.id.recyclerViewAchievements)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadAchievements()

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

    // Loads the user's achievements from the Fire db
    private fun loadAchievements() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val achievementsData = document.get("achievements") as? Map<String, Map<String, Any>> ?: emptyMap()

                    val achievementList = achievementsData.map {
                        Achievement(
                            name = it.key,
                            description = it.value["description"] as String,
                            unlocked = it.value["unlocked"] as Boolean
                        )
                    }
                    achievementAdapter = AchievementAdapter(achievementList)
                    recyclerView.adapter = achievementAdapter
                }
            }
        }
    }
}
