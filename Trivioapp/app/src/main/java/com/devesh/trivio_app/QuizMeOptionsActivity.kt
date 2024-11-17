package com.devesh.trivio_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class QuizMeOptionsActivity : AppCompatActivity() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quizoptions)

        val spinner = findViewById<Spinner>(R.id.spinner_number_of_questions)
        val btnStartQuiz = findViewById<Button>(R.id.btn_start_quiz)
        val switchQuestionType = findViewById<Switch>(R.id.switch_question_type)
        val categorySpinner = findViewById<Spinner>(R.id.spinner_topic)

        val topics = arrayOf(
            "Select a topic",
            "General Knowledge", "Entertainment: Books", "Entertainment: Film", "Entertainment: Music",
            "Entertainment: Television", "Entertainment: Video Games", "Entertainment: Board Games",
            "Science: Computers", "Science: Mathematics", "Mythology", "Sports", "Geography",
            "History", "Politics", "Art", "Celebrities", "Animals", "Vehicles"
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

                    savePreferences(numberOfQuestions, selectedCategoryIndex, switchQuestionType.isChecked)

                    startActivity(intent)

                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid number selected!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select a valid number of questions and a category.", Toast.LENGTH_SHORT).show()
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

    private fun savePreferences(numberOfQuestions: Int, selectedCategoryIndex: Int, isSwitchChecked: Boolean) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("QuizPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putInt("NUMBER_OF_QUESTIONS", numberOfQuestions)
        editor.putInt("SELECTED_CATEGORY", selectedCategoryIndex)
        editor.putBoolean("SWITCH_STATE", isSwitchChecked)

        editor.apply()
    }

    private fun loadPreferences() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("QuizPreferences", Context.MODE_PRIVATE)
        val savedNumberOfQuestions = sharedPreferences.getInt("NUMBER_OF_QUESTIONS", 0)  // Default to 5 if not found
        val savedCategoryIndex = sharedPreferences.getInt("SELECTED_CATEGORY", 0)  // Default to "Select a topic"
        val savedSwitchState = sharedPreferences.getBoolean("SWITCH_STATE", true)  // Default to True for TF Quiz
        val spinner = findViewById<Spinner>(R.id.spinner_number_of_questions)
        val categorySpinner = findViewById<Spinner>(R.id.spinner_topic)
        val switchQuestionType = findViewById<Switch>(R.id.switch_question_type)
        val numberOfQuestions = arrayOf("Select a number", "5", "10", "15", "20")  // Example
        val numberOfQuestionsPosition = numberOfQuestions.indexOf(savedNumberOfQuestions.toString())
        spinner.setSelection(numberOfQuestionsPosition)

        if (savedCategoryIndex != 0) {
            categorySpinner.setSelection(savedCategoryIndex)
        } else {
            categorySpinner.setSelection(0)
        }
        switchQuestionType.isChecked = savedSwitchState
    }
}
