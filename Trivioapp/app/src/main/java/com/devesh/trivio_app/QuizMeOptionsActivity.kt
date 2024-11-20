package com.devesh.trivio_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

class QuizMeOptionsActivity : AppCompatActivity() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_quizoptions)

        val spinner = findViewById<Spinner>(R.id.spinner_number_of_questions)
        val btnStartQuiz = findViewById<Button>(R.id.btn_start_quiz)
        val switchQuestionType = findViewById<Switch>(R.id.switch_question_type)
        val categorySpinner = findViewById<Spinner>(R.id.spinner_topic)

        val topics = arrayOf(
            "Select a topic",
            "General Knowledge",
            "Entertainment: Books",
            "Entertainment: Film",
            "Entertainment: Music",
            "Entertainment: Television",
            "Entertainment: Video Games",
            "Entertainment: Board Games",
            "Science: Computers",
            "Science: Mathematics",
            "Mythology",
            "Sports",
            "Geography",
            "History",
            "Politics",
            "Art",
            "Celebrities",
            "Animals",
            "Vehicles"
        )

        val categoryIds = arrayOf(
            -1, 9, 10, 11, 12, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, topics)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        loadPreferences()

        btnStartQuiz.setOnClickListener {
            val selectedItem = spinner.selectedItem.toString()

            if (selectedItem != "Select a number" && categorySpinner.selectedItem != null && categorySpinner.selectedItem != "Select a topic") {
                try {
                    val numberOfQuestions = selectedItem.toInt()
                    val selectedCategoryIndex = categorySpinner.selectedItemPosition
                    val categoryId = categoryIds[selectedCategoryIndex]
                    val intent: Intent = if (switchQuestionType.isChecked) {
                        Intent(this, TFQuizActivity::class.java)
                    } else {
                        Intent(this, MCQQuizActivity::class.java)
                    }
                    intent.putExtra("NUMBER_OF_QUESTIONS", numberOfQuestions)
                    intent.putExtra("CATEGORY_ID", categoryId)

                    savePreferences(
                        numberOfQuestions,
                        selectedCategoryIndex,
                        switchQuestionType.isChecked
                    )

                    updatePressCountAndAchievements()

                    startActivity(intent)

                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid number selected!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Please select a valid number of questions and a category.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.action_quiz -> {
                    true
                }

                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    private fun savePreferences(
        numberOfQuestions: Int,
        selectedCategoryIndex: Int,
        isSwitchChecked: Boolean
    ) {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("QuizPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putInt("NUMBER_OF_QUESTIONS", numberOfQuestions)
        editor.putInt("SELECTED_CATEGORY", selectedCategoryIndex)
        editor.putBoolean("SWITCH_STATE", isSwitchChecked)

        editor.apply()
    }

    private fun loadPreferences() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("QuizPreferences", Context.MODE_PRIVATE)
        val savedNumberOfQuestions =
            sharedPreferences.getInt("NUMBER_OF_QUESTIONS", 0)
        val savedCategoryIndex =
            sharedPreferences.getInt("SELECTED_CATEGORY", 0)
        val savedSwitchState =
            sharedPreferences.getBoolean("SWITCH_STATE", true)
        val spinner = findViewById<Spinner>(R.id.spinner_number_of_questions)
        val categorySpinner = findViewById<Spinner>(R.id.spinner_topic)
        val switchQuestionType = findViewById<Switch>(R.id.switch_question_type)
        val numberOfQuestions = arrayOf("Select a number", "5", "10", "15", "20")
        val numberOfQuestionsPosition = numberOfQuestions.indexOf(savedNumberOfQuestions.toString())
        spinner.setSelection(numberOfQuestionsPosition)

        if (savedCategoryIndex != 0) {
            categorySpinner.setSelection(savedCategoryIndex)
        } else {
            categorySpinner.setSelection(0)
        }
        switchQuestionType.isChecked = savedSwitchState
    }

    private fun updatePressCountAndAchievements() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentPressCount = document.getLong("pressCount") ?: 0
                    val newPressCount = currentPressCount + 1

                    val achievements = mutableMapOf<String, Map<String, Any>>()
                    val unlockedAchievements = mutableListOf<String>()

                    achievements["First Quiz"] = mapOf(
                        "unlocked" to (newPressCount >= 1),
                        "description" to "Complete your first quiz!"
                    )
                    achievements["Quiz Newbie"] = mapOf(
                        "unlocked" to (newPressCount >= 5),
                        "description" to "Complete 5 quizzes!"
                    )
                    achievements["Knowledge Seeker"] = mapOf(
                        "unlocked" to (newPressCount >= 20),
                        "description" to "Complete 20 quizzes!"
                    )
                    achievements["Half-Century Hero"] = mapOf(
                        "unlocked" to (newPressCount >= 50),
                        "description" to "Complete 50 quizzes!"
                    )
                    achievements["Mastermind"] = mapOf(
                        "unlocked" to (newPressCount >= 100),
                        "description" to "Complete 100 quizzes!"
                    )

                    achievements.forEach { (achievement, details) ->
                        val unlocked = details["unlocked"] as Boolean
                        val previouslyUnlocked = document.get("achievements.$achievement.unlocked") as? Boolean ?: false

                        if (unlocked && !previouslyUnlocked) {
                            Toast.makeText(applicationContext, "$achievement Unlocked!", Toast.LENGTH_SHORT).show()
                            unlockedAchievements.add(achievement)
                        }
                    }

                    val updatedAchievements = achievements.mapValues { entry ->
                        if (unlockedAchievements.contains(entry.key)) {
                            entry.value + ("unlocked" to true)
                        } else {
                            entry.value
                        }
                    }

                    val updates = mapOf(
                        "pressCount" to newPressCount,
                        "achievements" to updatedAchievements
                    )

                    userRef.update(updates)

                } else {
                    val achievements = mutableMapOf<String, Map<String, Any>>(
                        "First Quiz" to mapOf(
                            "unlocked" to false,
                            "description" to "Complete your first quiz!"
                        ),
                        "Quiz Newbie" to mapOf(
                            "unlocked" to false,
                            "description" to "Complete 5 quizzes!"
                        ),
                        "Knowledge Seeker" to mapOf(
                            "unlocked" to false,
                            "description" to "Complete 20 quizzes!"
                        ),
                        "Half-Century Hero" to mapOf(
                            "unlocked" to false,
                            "description" to "Complete 50 quizzes!"
                        ),
                        "Mastermind" to mapOf(
                            "unlocked" to false,
                            "description" to "Complete 100 quizzes!"
                        )
                    )

                    val userData = mapOf(
                        "pressCount" to 1,
                        "achievements" to achievements
                    )

                    userRef.set(userData)
                }
            }
        }
    }
}
