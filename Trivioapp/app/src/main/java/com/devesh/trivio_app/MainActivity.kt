package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        FirebaseApp.initializeApp(this)

        val sharedPreferences = getSharedPreferences("SettingsPreferences", MODE_PRIVATE)
        val sharedPreferences1 = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isMusicEnabled = sharedPreferences.getBoolean("music_enabled", false)

        if (isMusicEnabled) {
            MusicManager.startMusic(this, R.raw.background)
        }

        val username = sharedPreferences1.getString("USERNAME", "Guest")
        val welcomeTextView: TextView = findViewById(R.id.tv_welcome)
        welcomeTextView.text = "Welcome, $username"

        val btnQuizMe: Button = findViewById(R.id.btn_quiz_me)
        val btnLeaderboard: Button = findViewById(R.id.btn_leaderboard)
        val btnPast: Button = findViewById(R.id.btn_past)
        val btnAchievements: Button = findViewById(R.id.btn_achievements)

        btnQuizMe.setOnClickListener {
            startActivity(Intent(this, QuizMeOptionsActivity::class.java))
        }

        btnLeaderboard.setOnClickListener {
        }

        btnPast.setOnClickListener {
            startActivity(Intent(this, PastResponsesActivity::class.java))
        }

        btnAchievements.setOnClickListener {
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_home -> true
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

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("SettingsPreferences", MODE_PRIVATE)
        val isMusicEnabled = sharedPreferences.getBoolean("music_enabled", false)

        if (isMusicEnabled) {
            MusicManager.resumeMusic()
        } else {
            MusicManager.stopMusic()
        }
    }
}
