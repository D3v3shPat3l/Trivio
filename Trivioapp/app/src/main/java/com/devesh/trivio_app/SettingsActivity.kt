package com.devesh.trivio_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import java.util.concurrent.TimeUnit

// Activity that manages the app's settings, including music, notifications, sound effects, and notification time
class SettingsActivity : AppCompatActivity() {

    private lateinit var musicSwitch: Switch
    private lateinit var notificationsSwitch: Switch
    private lateinit var soundEffectsSwitch: Switch
    private lateinit var timePicker: TimePicker
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var logoutButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    // onCreate is called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_settings)

        musicSwitch = findViewById(R.id.switch_music)
        notificationsSwitch = findViewById(R.id.switch_notifications)
        soundEffectsSwitch = findViewById(R.id.switch_sound_effects)
        timePicker = findViewById(R.id.timePicker)
        saveButton = findViewById(R.id.btn_save)
        cancelButton = findViewById(R.id.btn_cancel)
        logoutButton = findViewById(R.id.btn_logout)
        sharedPreferences = getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE)

        loadPreferences()

        saveButton.setOnClickListener {
            savePreferences()
        }

        cancelButton.setOnClickListener {
            loadPreferences()
            showToast("Changes reverted to last saved settings!")
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Set up bottom navigation to allow navigation between different screens
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.action_quiz -> {
                    startActivity(Intent(this, QuizMeOptionsActivity::class.java))
                    finish()
                    true
                }
                R.id.action_settings -> true
                else -> false
            }
        }
    }

    // Load saved preferences from SharedPreferences and update UI components
    private fun loadPreferences() {
        val isMusicEnabled = sharedPreferences.getBoolean("music_enabled", false)
        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        val isSoundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", false)
        val notificationTime = sharedPreferences.getLong("notification_time", 0)
        musicSwitch.isChecked = isMusicEnabled
        notificationsSwitch.isChecked = isNotificationsEnabled
        soundEffectsSwitch.isChecked = isSoundEffectsEnabled

        if (notificationTime != 0L) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = notificationTime
            timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = calendar.get(Calendar.MINUTE)
        }
    }

    // Save the current settings to SharedPreferences
    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("music_enabled", musicSwitch.isChecked)
        editor.putBoolean("notifications_enabled", notificationsSwitch.isChecked)
        editor.putBoolean("sound_effects_enabled", soundEffectsSwitch.isChecked)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
        calendar.set(Calendar.MINUTE, timePicker.minute)
        editor.putLong("notification_time", calendar.timeInMillis)

        editor.apply()
        showToast("Settings saved successfully!")

        if (notificationsSwitch.isChecked) {
            scheduleDailyNotification(calendar)
        } else {
            cancelDailyNotification()
        }
    }

    // Schedule a daily notification at the time selected by the user
    private fun scheduleDailyNotification(calendar: Calendar) {
        val notificationTime = calendar.timeInMillis

        val notificationRequest = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .setInitialDelay(notificationTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("notification_time" to notificationTime))
            .build()

        WorkManager.getInstance(this).enqueue(notificationRequest)
    }

    // Cancel any scheduled daily notifications
    private fun cancelDailyNotification() {
        WorkManager.getInstance(this).cancelAllWorkByTag("daily_notification")
    }

    // Show a toast message to the user
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
